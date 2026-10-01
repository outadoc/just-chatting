//
//  AnimatedImageView.swift
//  JustChatting
//

import Gifu
import Nuke
import SwiftUI

extension EnvironmentValues {
    /// Whether emotes (and badges) should play their animation, as set in the app's settings.
    @Entry var animateEmotes: Bool = true
    /// Whether GIFs posted in chat should play their animation, as set in the app's settings.
    @Entry var animateGifs: Bool = true
}

struct AnimatedImageView: UIViewRepresentable {
    let url: URL?
    /// When false, only the first frame of an animated image is shown.
    var animated: Bool = true
    var onImageLoaded: ((CGSize) -> Void)? = nil

    final class Coordinator {
        var loadedURL: URL?
        var loadedAnimated: Bool?
        var task: ImageTask?
        var onImageLoaded: ((CGSize) -> Void)?
    }

    func makeCoordinator() -> Coordinator { Coordinator() }

    func makeUIView(context: Context) -> GIFImageView {
        let view = GIFImageView()
        view.contentMode = .scaleAspectFit
        view.setContentHuggingPriority(.defaultLow, for: .horizontal)
        view.setContentHuggingPriority(.defaultLow, for: .vertical)
        view.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        view.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
        return view
    }

    func updateUIView(_ uiView: GIFImageView, context: Context) {
        let coordinator = context.coordinator
        coordinator.onImageLoaded = onImageLoaded
        guard url != coordinator.loadedURL || animated != coordinator.loadedAnimated else { return }
        coordinator.task?.cancel()
        coordinator.task = nil
        coordinator.loadedURL = url
        coordinator.loadedAnimated = animated

        // Lazy containers reuse this view for other images: clear the previous one right away.
        // Stopping the animation matters most, since a running animator keeps drawing the
        // previous GIF's frames, even over a static image set afterwards.
        uiView.prepareForReuse()
        uiView.image = nil

        guard let url else { return }

        coordinator.task = ImagePipeline.shared.loadImage(with: url) { [weak uiView] result in
            guard case let .success(response) = result else { return }
            DispatchQueue.main.async {
                // The view may have been given another URL since this request started.
                guard let uiView, coordinator.loadedURL == url, coordinator.loadedAnimated == animated else { return }
                if animated, let data = response.container.data {
                    uiView.animate(withGIFData: data)
                } else {
                    uiView.image = response.image
                }
                coordinator.onImageLoaded?(response.image.size)
            }
        }
    }

    static func dismantleUIView(_ uiView: GIFImageView, coordinator: Coordinator) {
        coordinator.task?.cancel()
        uiView.prepareForReuse()
    }
}
