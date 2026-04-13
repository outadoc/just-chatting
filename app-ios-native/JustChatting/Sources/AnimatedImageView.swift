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
        return view
    }

    func updateUIView(_ uiView: GIFImageView, context: Context) {
        context.coordinator.onImageLoaded = onImageLoaded
        guard url != context.coordinator.loadedURL else { return }
        context.coordinator.task?.cancel()
        context.coordinator.loadedURL = url
        guard let url else {
            uiView.prepareForReuse()
            return
        }

        let coordinator = context.coordinator
        coordinator.task = ImagePipeline.shared.loadImage(with: url) { result in
            guard case let .success(response) = result else { return }
            DispatchQueue.main.async {
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
