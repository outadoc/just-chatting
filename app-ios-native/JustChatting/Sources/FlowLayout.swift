//
//  FlowLayout.swift
//  JustChatting
//

import SwiftUI

struct FlowLayout: Layout {
    var spacing: CGFloat = 4

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let width = proposal.replacingUnspecifiedDimensions().width
        let rows = makeRows(subviews: subviews, width: width)
        let height = rows.reduce(0) { acc, row in
            acc + row.height + (acc > 0 ? spacing : 0)
        }
        return CGSize(width: width, height: height)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let rows = makeRows(subviews: subviews, width: bounds.width)
        var y = bounds.minY
        for row in rows {
            var x = bounds.minX
            for item in row.items {
                let itemHeight = item.size.height
                item.subview.place(
                    at: CGPoint(x: x, y: y + (row.height - itemHeight) / 2),
                    proposal: ProposedViewSize(item.size)
                )
                x += item.size.width + spacing
            }
            y += row.height + spacing
        }
    }

    private struct RowItem {
        let subview: LayoutSubview
        let size: CGSize
    }

    private struct Row {
        var items: [RowItem] = []
        var height: CGFloat = 0
        var width: CGFloat = 0
    }

    private func makeRows(subviews: Subviews, width: CGFloat) -> [Row] {
        var rows: [Row] = []
        var currentRow = Row()

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            let neededWidth = currentRow.items.isEmpty
                ? size.width
                : currentRow.width + spacing + size.width

            if neededWidth > width, !currentRow.items.isEmpty {
                rows.append(currentRow)
                currentRow = Row()
            }

            let prevWidth = currentRow.width
            let prevCount = currentRow.items.count
            currentRow.items.append(RowItem(subview: subview, size: size))
            currentRow.width = prevCount > 0 ? prevWidth + spacing + size.width : size.width
            currentRow.height = max(currentRow.height, size.height)
        }

        if !currentRow.items.isEmpty {
            rows.append(currentRow)
        }

        return rows
    }
}
