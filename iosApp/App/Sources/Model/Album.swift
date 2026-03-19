import Foundation
import SwiftData

@Model
final class Album {
    @Attribute(.unique) var id: String
    var name: String
    var sortOrder: Int
    var createdAt: Date
    var updatedAt: Date

    @Relationship(deleteRule: .cascade, inverse: \PhotoRef.album)
    var photos: [PhotoRef]

    init(name: String, sortOrder: Int = 0) {
        self.id = UUID().uuidString
        self.name = name
        self.sortOrder = sortOrder
        self.createdAt = Date()
        self.updatedAt = Date()
        self.photos = []
    }
}

@Model
final class PhotoRef {
    @Attribute(.unique) var id: String
    var platformAssetId: String
    var sortOrder: Int
    var addedAt: Date
    var album: Album?

    init(platformAssetId: String, sortOrder: Int = 0, album: Album) {
        self.id = UUID().uuidString
        self.platformAssetId = platformAssetId
        self.sortOrder = sortOrder
        self.addedAt = Date()
        self.album = album
    }
}
