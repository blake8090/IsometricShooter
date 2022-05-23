package bke.iso.util

data class Point(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
)

fun cartesianToIsometric(point: Point): Point =
    Point(
        point.x - point.y,
        (point.x + point.y) / 2f,
        point.z
    )

fun isometricToCartesian(point: Point): Point =
    Point(
        point.x - point.y,
        (point.x + point.y) / 2f,
        point.z
    )
