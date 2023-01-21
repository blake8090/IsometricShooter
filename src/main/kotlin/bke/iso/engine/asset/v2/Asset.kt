package bke.iso.engine.asset.v2

data class Asset<T>(
    val name: String,
    val fileName: String,
    val canonicalPath: String,
    val extension: String,
    val packageName: String,
    val value: T
)
