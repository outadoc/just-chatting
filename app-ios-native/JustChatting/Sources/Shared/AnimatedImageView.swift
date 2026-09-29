//
//  AnimatedImageView.swift
//  JustChatting
//

import Gifu
import Nuke
import SwiftUI

struct AnimatedImageView: UIViewRepresentable {
    let url: URL?
    var onImageLoaded: ((CGSize) -> Void)? = nil

    final class Coordinator {
        var loadedURL: URL?
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
        guard url != coordinator.loadedURL else { return }
        coordinator.task?.cancel()
        coordinator.task = nil
        coordinator.loadedURL = url

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
                guard let uiView, coordinator.loadedURL == url else { return }
                if let data = response.container.data {
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
