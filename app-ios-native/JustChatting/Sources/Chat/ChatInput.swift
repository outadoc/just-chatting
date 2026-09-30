//
//  ChatInput.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Message composer. `ChatViewModel.inputState` is the source of truth; the local text and
/// selection mirror it so that SwiftUI's TextField can be driven both by the user and by the
/// view model (autocomplete, reusing the last message, clearing after send).
struct ChatInput: View {
    let viewModel: ChatViewModel
    let inputState: ChatViewModel.InputState
    @Binding var isEmotePickerOpen: Bool

    @State private var text = ""
    @State private var selection: TextSelection?
    @FocusState private var isFocused: Bool

    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.displayScale) private var displayScale

    var body: some View {
        VStack(spacing: 8) {
            if let replyingTo = inputState.replyingTo?.body {
                replyBanner(replyingTo: replyingTo)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }

            if !inputState.autoCompleteItems.isEmpty {
                ChatAutoCompleteRow(
                    items: Array(inputState.autoCompleteItems),
                    onChatterClick: { chatter in
                        viewModel.appendChatter(chatter: chatter, autocomplete: true)
                    },
                    onEmoteClick: { emote in
                        viewModel.appendEmote(emote: emote, autocomplete: true)
                    }
                )
                .transition(.opacity)
            }

            GlassEffectContainer {
                composer
            }
        }
        .animation(.default, value: inputState.replyingTo)
        .animation(.default, value: inputState.autoCompleteItems.isEmpty)
        .onAppear(perform: pullFromViewModel)
        .onChange(of: inputState) { _, _ in pullFromViewModel() }
        .onChange(of: text) { _, newText in
            // Chat messages are single-line: treat Return as "send".
            if newText.contains(where: \.isNewline) {
                let message = newText.filter { !$0.isNewline }
                let end = Int32(message.utf16.count)
                viewModel.onMessageInputChanged(
                    message: message,
                    selectionRange: KotlinIntRange(start: end, endInclusive: end)
                )
                submit()
                // Mirror the view model's state (cleared after sending) rather than pushing ours.
                pullFromViewModel()
                return
            }
            pushToViewModel()
        }
        .onChange(of: selection) { _, _ in pushToViewModel() }
        .onChange(of: isFocused) { _, focused in
            if focused {
                isEmotePickerOpen = false
            }
        }
    }

    /// Emote button, text field and send button.
    private var composer: some View {
        HStack(alignment: .bottom, spacing: 8) {
            Button {
                isEmotePickerOpen.toggle()
                // Swap between the emote picker and the keyboard.
                isFocused = !isEmotePickerOpen
            } label: {
                Image(systemName: isEmotePickerOpen ? "keyboard" : "face.smiling")
                    .font(.title3)
                    .frame(width: 36, height: 36)
            }
            .buttonStyle(.glass)
            .buttonBorderShape(.circle)
            .accessibilityLabel(isEmotePickerOpen ? "Show keyboard" : "Show emotes")

            HStack(alignment: .center, spacing: 4) {
                // The built-in placeholder wraps like the text it stands for; draw our own so that
                // it stays on a single line, truncated if needed.
                TextField("", text: $text, selection: $selection, axis: .vertical)
                    .textFieldStyle(.plain)
                    .lineLimit(1...5)
                    .overlay(alignment: .leading) {
                        if text.isEmpty {
                            Text("Send a message")
                                .lineLimit(1)
                                .truncationMode(.tail)
                                .foregroundStyle(Color(uiColor: .placeholderText))
                                .allowsHitTesting(false)
                                .accessibilityHidden(true)
                        }
                    }
                    .accessibilityLabel("Send a message")
                    .focused($isFocused)
                    .submitLabel(.send)
                    .onKeyPress(.tab) {
                        viewModel.onTriggerAutoComplete()
                        return .handled
                    }

                trailingButton
            }
            .frame(maxHeight: .infinity)
            .padding(.leading, 16)
            .padding(.trailing, 6)
            .frame(minHeight: 48)
            .glassEffect(.regular.interactive(), in: RoundedRectangle(cornerRadius: 24))

            Button(action: submit) {
                Image(systemName: "arrow.up")
                    .fontWeight(.semibold)
                    .frame(width: 36, height: 36)
            }
            .buttonStyle(.glassProminent)
            .buttonBorderShape(.circle)
            .disabled(text.isEmpty)
            .accessibilityLabel("Send")
        }
        .fixedSize(horizontal: false, vertical: true)
    }

    @ViewBuilder
    private var trailingButton: some View {
        if !text.isEmpty {
            Button {
                viewModel.onMessageInputChanged(
                    message: "",
                    selectionRange: KotlinIntRange(start: 0, endInclusive: 0)
                )
            } label: {
                Image(systemName: "xmark.circle.fill")
                    .foregroundStyle(.secondary)
                    .frame(width: 28, height: 32)
            }
            .accessibilityLabel("Clear message")
        } else if inputState.canReuseLastMessage {
            Button {
                viewModel.onReuseLastMessageClicked()
            } label: {
                Image(systemName: "arrow.uturn.backward")
                    .foregroundStyle(.secondary)
                    .frame(width: 28, height: 32)
            }
            .accessibilityLabel("Reuse last message")
        }
    }

    private func replyBanner(replyingTo: ChatListItemMessage.Body) -> some View {
        HStack(spacing: 8) {
            InReplyToMessage(
                mentions: [replyingTo.chatter.displayName],
                message: replyingTo.message
            )

            Button {
                viewModel.onReplyToMessage(entry: nil)
            } label: {
                Image(systemName: "xmark")
                    .foregroundStyle(.secondary)
            }
            .accessibilityLabel("Cancel reply")
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 12))
    }

    private func submit() {
        guard !viewModel.inputState.value.message.isEmpty else { return }
        viewModel.submit(
            screenDensity: Float(displayScale),
            isDarkTheme: colorScheme == .dark
        )
    }

    // MARK: - Syncing with the view model

    /// Sends the local text and selection to the view model, unless they already match. Echoing
    /// back a view model-driven change would reset its autocomplete state.
    private func pushToViewModel() {
        let range = utf16Range(of: selection, in: text)
        let current = viewModel.inputState.value
        if current.message == text,
           Int(current.selectionRange.first) == range.lowerBound,
           Int(current.selectionRange.last) == range.upperBound {
            return
        }
        viewModel.onMessageInputChanged(
            message: text,
            selectionRange: KotlinIntRange(
                start: Int32(range.lowerBound),
                endInclusive: Int32(range.upperBound)
            )
        )
    }

    /// Applies view model-driven changes to the text field. Reads the current value rather than
    /// the emitted one, since the user may have typed more since then.
    private func pullFromViewModel() {
        let current = viewModel.inputState.value
        guard current.message != text else { return }
        text = current.message
        // Set the selection in the same update so that it's never out of bounds for the new text.
        let cursor = stringIndex(utf16Offset: Int(current.selectionRange.last), in: text)
        selection = TextSelection(insertionPoint: cursor)
    }

    // Kotlin strings are indexed in UTF-16 code units; the selection range's "last" bound is
    // exclusive, matching Compose's TextRange.
    private func utf16Range(of selection: TextSelection?, in text: String) -> Range<Int> {
        guard let selection, case .selection(let range) = selection.indices else {
            let end = text.utf16.count
            return end..<end
        }
        // The selection may briefly refer to a previous, longer text: clamp it.
        let upperIndex = min(range.upperBound, text.endIndex)
        let lowerIndex = min(range.lowerBound, upperIndex)
        let lower = text.utf16.distance(from: text.startIndex, to: lowerIndex)
        let upper = text.utf16.distance(from: text.startIndex, to: upperIndex)
        return lower..<upper
    }

    private func stringIndex(utf16Offset: Int, in text: String) -> String.Index {
        let offset = min(max(utf16Offset, 0), text.utf16.count)
        return String.Index(utf16Offset: offset, in: text)
    }
}
