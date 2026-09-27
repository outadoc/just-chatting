//
//  SharedViewModel.swift
//  JustChatting
//

import JCShared
import SwiftUI

/// Gives a view its own Kotlin ViewModel, the way `koinViewModel()` does in Compose: it is created
/// the first time the view reads it, kept for as long as the view's identity lives, and cleared
/// afterwards, which cancels its `viewModelScope`.
///
///     @SharedViewModel(\.chatViewModel) private var viewModel
@propertyWrapper
struct SharedViewModel<ViewModel: AnyObject>: DynamicProperty {
    @State private var holder: Holder

    init(_ keyPath: KeyPath<ViewModelOwner, ViewModel>) {
        _holder = State(initialValue: Holder(keyPath: keyPath))
    }

    var wrappedValue: ViewModel {
        holder.viewModel
    }

    /// `@State` evaluates its initial value every time the view is initialized, and only keeps the
    /// first one. Creating the ViewModel lazily means that the discarded holders never create one.
    private final class Holder {
        private let owner = ViewModelOwner()
        private let keyPath: KeyPath<ViewModelOwner, ViewModel>

        init(keyPath: KeyPath<ViewModelOwner, ViewModel>) {
            self.keyPath = keyPath
        }

        lazy var viewModel: ViewModel = owner[keyPath: keyPath]

        deinit {
            owner.clear()
        }
    }
}
