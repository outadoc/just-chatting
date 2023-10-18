//
//  AvatarView.swift
//  just-chatting
//
//  Created by Baptiste Candellier on 2023-10-18.
//  Copyright © 2023 Baptiste Candellier. All rights reserved.
//

import SwiftUI

struct AvatarView: View {
    var url: URL
    var size: CGFloat = 56
    var body: some View {
        AsyncImage(
            url: url,
            content: { image in
                image.resizable()
                    .aspectRatio(1, contentMode: .fit)
                    .frame(width: size, height: size)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            },
            placeholder: {
                ProgressView()
                    .frame(width: size, height: size)
            }
        )
    }
}
