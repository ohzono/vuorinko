import SwiftUI

struct PinDotsView: View {
    let length: Int
    let total: Int

    var body: some View {
        HStack(spacing: 16) {
            ForEach(0..<total, id: \.self) { index in
                Circle()
                    .fill(index < length ? Color.accentColor : Color.gray.opacity(0.3))
                    .frame(width: 20, height: 20)
            }
        }
    }
}

struct NumberPadView: View {
    let onDigit: (Int) -> Void
    let onDelete: () -> Void
    let enabled: Bool

    private let rows: [[Int]] = [
        [1, 2, 3],
        [4, 5, 6],
        [7, 8, 9],
        [-1, 0, -2],
    ]

    var body: some View {
        VStack(spacing: 12) {
            ForEach(rows, id: \.self) { row in
                HStack(spacing: 24) {
                    ForEach(row, id: \.self) { key in
                        if key == -1 {
                            Color.clear.frame(width: 72, height: 72)
                        } else if key == -2 {
                            Button(action: onDelete) {
                                Image(systemName: "delete.backward")
                                    .font(.title2)
                                    .frame(width: 72, height: 72)
                            }
                            .disabled(!enabled)
                        } else {
                            Button(action: { onDigit(key) }) {
                                Text("\(key)")
                                    .font(.title)
                                    .frame(width: 72, height: 72)
                                    .background(Color(.systemGray5))
                                    .clipShape(Circle())
                            }
                            .disabled(!enabled)
                        }
                    }
                }
            }
        }
    }
}
