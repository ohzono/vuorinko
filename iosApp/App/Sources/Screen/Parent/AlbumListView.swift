import SwiftUI
import SwiftData

struct AlbumListView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Album.sortOrder) private var albums: [Album]
    @State private var showCreateDialog = false
    @State private var newAlbumName = ""
    @State private var deleteTarget: Album?

    let onAlbumTap: (String) -> Void
    let onChildMode: (String) -> Void

    var body: some View {
        NavigationStack {
            albumContent
                .navigationTitle("Vuorinko")
                .toolbar {
                    ToolbarItem(placement: .primaryAction) {
                        Button(action: { showCreateDialog = true }) {
                            Image(systemName: "plus")
                        }
                    }
                }
                .alert("新しいアルバム", isPresented: $showCreateDialog) {
                    TextField("アルバム名", text: $newAlbumName)
                    Button("作成") { createAlbum() }
                    Button("キャンセル", role: .cancel) { newAlbumName = "" }
                }
                .alert("アルバムを削除", isPresented: deleteAlertBinding) {
                    Button("削除", role: .destructive) { performDelete() }
                    Button("キャンセル", role: .cancel) { deleteTarget = nil }
                } message: {
                    Text(deleteTarget.map { "「\($0.name)」を削除しますか？" } ?? "")
                }
        }
    }

    @ViewBuilder
    private var albumContent: some View {
        if albums.isEmpty {
            ContentUnavailableView(
                "アルバムがありません",
                systemImage: "photo.on.rectangle.angled",
                description: Text("右上の＋ボタンで作成")
            )
        } else {
            List {
                ForEach(albums) { album in
                    AlbumRow(album: album, onTap: onAlbumTap, onChildMode: onChildMode, onDelete: {
                        deleteTarget = album
                    })
                }
            }
        }
    }

    private var deleteAlertBinding: Binding<Bool> {
        Binding(
            get: { deleteTarget != nil },
            set: { if !$0 { deleteTarget = nil } }
        )
    }

    private func createAlbum() {
        guard !newAlbumName.isEmpty else { return }
        let name = String(newAlbumName.prefix(AppConfig.maxAlbumNameLength))
        let album = Album(name: name, sortOrder: albums.count)
        modelContext.insert(album)
        newAlbumName = ""
    }

    private func performDelete() {
        if let album = deleteTarget {
            modelContext.delete(album)
            deleteTarget = nil
        }
    }
}

private struct AlbumRow: View {
    let album: Album
    let onTap: (String) -> Void
    let onChildMode: (String) -> Void
    let onDelete: () -> Void

    var body: some View {
        Button(action: { onTap(album.id) }) {
            HStack {
                VStack(alignment: .leading) {
                    Text(album.name).font(.headline)
                    Text("\(album.photos.count)枚").font(.caption).foregroundStyle(.secondary)
                }
                Spacer()
                if !album.photos.isEmpty {
                    Button(action: { onChildMode(album.id) }) {
                        Image(systemName: "play.fill").foregroundStyle(Color.accentColor)
                    }
                    .buttonStyle(.borderless)
                }
            }
        }
        .foregroundStyle(.primary)
        .swipeActions(edge: .trailing) {
            Button(role: .destructive, action: onDelete) {
                Label("削除", systemImage: "trash")
            }
        }
    }
}
