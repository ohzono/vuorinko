import SwiftUI
import SwiftData
import PhotosUI

struct PhotoManageView: View {
    let albumId: String
    let onBack: () -> Void

    @Environment(\.modelContext) private var modelContext
    @State private var selectedItems: [PhotosPickerItem] = []
    @State private var deleteTarget: PhotoRef?

    private var album: Album? {
        let descriptor = FetchDescriptor<Album>(predicate: #Predicate { $0.id == albumId })
        return try? modelContext.fetch(descriptor).first
    }

    private var photos: [PhotoRef] {
        (album?.photos ?? []).sorted { $0.sortOrder < $1.sortOrder }
    }

    var body: some View {
        NavigationStack {
            Group {
                if photos.isEmpty {
                    ContentUnavailableView(
                        "写真がありません",
                        systemImage: "photo",
                        description: Text("右上のボタンで追加")
                    )
                } else {
                    ScrollView {
                        LazyVGrid(columns: [GridItem(.adaptive(minimum: 100), spacing: 4)], spacing: 4) {
                            ForEach(photos) { photo in
                                PhotoGridItem(photo: photo) {
                                    deleteTarget = photo
                                }
                            }
                        }
                        .padding(4)
                    }
                }
            }
            .navigationTitle("写真管理")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("戻る", action: onBack)
                }
                ToolbarItem(placement: .primaryAction) {
                    PhotosPicker(selection: $selectedItems,
                                 maxSelectionCount: 50,
                                 matching: .images) {
                        Image(systemName: "plus")
                    }
                }
            }
            .onChange(of: selectedItems) { _, newItems in
                addPhotos(from: newItems)
            }
            .alert("写真を削除", isPresented: Binding(
                get: { deleteTarget != nil },
                set: { if !$0 { deleteTarget = nil } }
            )) {
                Button("削除", role: .destructive) {
                    if let photo = deleteTarget {
                        modelContext.delete(photo)
                        deleteTarget = nil
                    }
                }
                Button("キャンセル", role: .cancel) { deleteTarget = nil }
            } message: {
                Text("この写真をアルバムから削除しますか？")
            }
        }
    }

    private func addPhotos(from items: [PhotosPickerItem]) {
        guard let album else { return }
        let startOrder = photos.count
        for (index, item) in items.enumerated() {
            let assetId = item.itemIdentifier ?? UUID().uuidString
            let photo = PhotoRef(
                platformAssetId: assetId,
                sortOrder: startOrder + index,
                album: album
            )
            modelContext.insert(photo)
        }
        selectedItems = []
    }
}

private struct PhotoGridItem: View {
    let photo: PhotoRef
    let onLongPress: () -> Void

    var body: some View {
        Rectangle()
            .fill(Color.gray.opacity(0.2))
            .aspectRatio(1, contentMode: .fill)
            .overlay {
                // PHAsset loading would go here
                Image(systemName: "photo")
                    .font(.title2)
                    .foregroundStyle(.secondary)
            }
            .clipShape(RoundedRectangle(cornerRadius: 4))
            .onLongPressGesture { onLongPress() }
    }
}
