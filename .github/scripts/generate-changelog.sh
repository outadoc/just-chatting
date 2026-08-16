#!/usr/bin/env bash
# Generates a changelog in Markdown, grouped by conventional commit type,
# from the commits between two refs.
#
# Usage: generate-changelog.sh <previous-ref-or-empty> <current-ref>
set -euo pipefail

PREV_REF="${1:-}"
CURRENT_REF="${2:-HEAD}"

if [[ -n "$PREV_REF" ]]; then
  RANGE="${PREV_REF}..${CURRENT_REF}"
else
  RANGE="${CURRENT_REF}"
fi

declare -A SECTION_TITLES=(
  [feat]="Features"
  [fix]="Bug Fixes"
  [perf]="Performance"
  [refactor]="Refactoring"
  [docs]="Documentation"
  [build]="Build"
  [ci]="CI"
  [test]="Tests"
  [style]="Style"
  [chore]="Chores"
  [revert]="Reverts"
)
SECTION_ORDER=(feat fix perf refactor docs build ci test style chore revert)

TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT

CONVENTIONAL_COMMIT_PATTERN='^([A-Za-z]+)(\(([^)]*)\))?!?:[[:space:]](.*)$'

while IFS= read -r subject; do
  [[ -z "$subject" ]] && continue

  if [[ "$subject" =~ $CONVENTIONAL_COMMIT_PATTERN ]]; then
    type="${BASH_REMATCH[1],,}"
    scope="${BASH_REMATCH[3]}"
    message="${BASH_REMATCH[4]}"
  else
    type="other"
    scope=""
    message="$subject"
  fi

  if [[ -z "${SECTION_TITLES[$type]:-}" ]]; then
    type="other"
  fi

  if [[ -n "$scope" ]]; then
    echo "- **${scope}:** ${message}" >> "$TMP_DIR/$type"
  else
    echo "- ${message}" >> "$TMP_DIR/$type"
  fi
done < <(git log "$RANGE" --no-merges --pretty=format:"%s")

FOUND_ANY=0
for type in "${SECTION_ORDER[@]}"; do
  if [[ -f "$TMP_DIR/$type" ]]; then
    FOUND_ANY=1
    echo "### ${SECTION_TITLES[$type]}"
    echo
    cat "$TMP_DIR/$type"
    echo
  fi
done

if [[ -f "$TMP_DIR/other" ]]; then
  FOUND_ANY=1
  echo "### Other Changes"
  echo
  cat "$TMP_DIR/other"
  echo
fi

if [[ "$FOUND_ANY" -eq 0 ]]; then
  echo "No notable changes."
fi
