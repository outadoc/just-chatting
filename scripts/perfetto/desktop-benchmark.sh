#!/usr/bin/env bash
# Runs the desktop app while Perfetto records system-wide process CPU/RAM
# samples, producing a .perfetto-trace file for benchmarking.
#
# Usage: scripts/perfetto/desktop-benchmark.sh [options]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
CACHE_DIR="$SCRIPT_DIR/.cache"
TRACES_DIR="$SCRIPT_DIR/traces"
TRACEBOX="$CACHE_DIR/tracebox"

GRADLE_TASK=":app-desktop:run"
POLL_MS=250
OUT_FILE=""

usage() {
  cat <<EOF
Usage: $(basename "$0") [options]

Launches the desktop app (via Gradle) while Perfetto's tracing daemons
record system-wide process CPU/RAM samples. Tracing stops automatically
when the app window is closed, the Gradle task exits, or you Ctrl+C this
script.

Options:
  -o, --output PATH   Where to write the .perfetto-trace file
                       (default: scripts/perfetto/traces/desktop-<timestamp>.perfetto-trace)
  -t, --task TASK      Gradle task used to launch the app (default: $GRADLE_TASK)
  -p, --poll-ms MS     Sampling interval for process/mem stats, in ms (default: $POLL_MS)
  -h, --help           Show this help

Notes:
  - Downloads Perfetto's self-contained 'tracebox' binary on first run
    (requires network access) and caches it under scripts/perfetto/.cache/.
  - Captures per-process RAM (RSS) and system-wide CPU load without root.
  - Per-thread/per-process CPU *scheduling* additionally requires ftrace
    access, which is root-only on most distros. Re-run as
    'sudo -E $0' to also capture that; otherwise CPU is only
    visible in aggregate (all cores combined) via /proc/stat sampling.
  - Open the resulting trace at https://ui.perfetto.dev, or explore it
    with the perfetto-sql / perfetto-trace-analysis skills.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -o|--output) OUT_FILE="$2"; shift 2 ;;
    -t|--task) GRADLE_TASK="$2"; shift 2 ;;
    -p|--poll-ms) POLL_MS="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "$CACHE_DIR" "$TRACES_DIR"

if [[ -z "$OUT_FILE" ]]; then
  OUT_FILE="$TRACES_DIR/desktop-$(date +%Y%m%d-%H%M%S).perfetto-trace"
fi

if [[ ! -x "$TRACEBOX" ]]; then
  echo "Downloading tracebox (Perfetto's standalone tracing binary)..."
  curl -fsSL https://get.perfetto.dev/tracebox -o "$TRACEBOX"
  chmod +x "$TRACEBOX"
fi

FTRACE_AVAILABLE=0
if [[ -r /sys/kernel/tracing/trace || -r /sys/kernel/debug/tracing/trace ]]; then
  FTRACE_AVAILABLE=1
else
  echo "Note: no ftrace access (not root) -- per-thread CPU scheduling won't be" >&2
  echo "recorded, only per-process RAM and system-wide CPU. Re-run with 'sudo -E $0' for full CPU data." >&2
fi

CONFIG_FILE="$(mktemp -t perfetto-desktop-config.XXXXXX.pbtx)"
{
  cat <<HEADER
buffers { size_kb: 65536 fill_policy: RING_BUFFER }
buffers { size_kb: 4096 fill_policy: RING_BUFFER }

data_sources {
  config {
    name: "linux.process_stats"
    target_buffer: 0
    process_stats_config {
      scan_all_processes_on_start: true
      proc_stats_poll_ms: ${POLL_MS}
      record_thread_names: true
    }
  }
}

data_sources {
  config {
    name: "linux.sys_stats"
    target_buffer: 1
    sys_stats_config {
      meminfo_period_ms: ${POLL_MS}
      meminfo_counters: MEMINFO_MEM_TOTAL
      meminfo_counters: MEMINFO_MEM_FREE
      meminfo_counters: MEMINFO_MEM_AVAILABLE
      meminfo_counters: MEMINFO_BUFFERS
      meminfo_counters: MEMINFO_CACHED
      meminfo_counters: MEMINFO_SWAP_FREE
      meminfo_counters: MEMINFO_SWAP_TOTAL
      vmstat_period_ms: ${POLL_MS}
      vmstat_counters: VMSTAT_PGFAULT
      vmstat_counters: VMSTAT_PGMAJFAULT
      stat_period_ms: ${POLL_MS}
      stat_counters: STAT_CPU_TIMES
      stat_counters: STAT_FORK_COUNT
    }
  }
}
HEADER

  if [[ "$FTRACE_AVAILABLE" -eq 1 ]]; then
    cat <<FTRACE
data_sources {
  config {
    name: "linux.ftrace"
    target_buffer: 0
    ftrace_config {
      ftrace_events: "sched/sched_switch"
      ftrace_events: "sched/sched_waking"
      ftrace_events: "sched/sched_process_exit"
      ftrace_events: "sched/sched_process_free"
      ftrace_events: "task/task_newtask"
      ftrace_events: "task/task_rename"
      compact_sched { enabled: true }
    }
  }
}
FTRACE
  fi

  cat <<FOOTER
write_into_file: true
file_write_period_ms: 2000
FOOTER
} > "$CONFIG_FILE"

cleanup() {
  if [[ -n "${TRACEBOX_PID:-}" ]] && kill -0 "$TRACEBOX_PID" 2>/dev/null; then
    kill -TERM "$TRACEBOX_PID" 2>/dev/null || true
    wait "$TRACEBOX_PID" 2>/dev/null || true
  fi
  rm -f "$CONFIG_FILE"
}
trap cleanup EXIT INT TERM

"$TRACEBOX" -c "$CONFIG_FILE" --txt -o "$OUT_FILE" &
TRACEBOX_PID=$!
sleep 1 # let tracing start before launching the app

echo "Recording Perfetto trace to $OUT_FILE"
echo "Launching desktop app ($GRADLE_TASK) -- close the app window to stop tracing."
echo

(cd "$REPO_ROOT" && ./gradlew "$GRADLE_TASK") || true

echo
echo "App exited, stopping trace capture..."
cleanup
trap - EXIT INT TERM

echo "Trace written to: $OUT_FILE"
echo "Open it at https://ui.perfetto.dev, or explore it with the perfetto-sql / perfetto-trace-analysis skills."
