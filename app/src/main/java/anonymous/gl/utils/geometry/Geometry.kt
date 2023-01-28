package anonymous.gl.utils.geometry

object Geometry {
    fun distanceBetween(point: Point, ray: Ray): Float {
        val p1ToPoint = vectorBetween(ray.point, point)
        val p2ToPoint = vectorBetween(ray.point.translate(ray.vector), point)

        val areaOfTriangleTimesTwo = p1ToPoint.crossProduct(p2ToPoint).length
        val lengthOfBase = ray.vector.length

        return areaOfTriangleTimesTwo / lengthOfBase
    }

    fun vectorBetween(from: Point, to: Point): Vector {
        return Vector(
            to.x - from.x,
            to.y - from.y,
            to.z - from.z,
        )
    }

    fun intersects(sphere: Sphere, ray: Ray): Boolean {
        return distanceBetween(sphere.center, ray) < sphere.radius
    }

    fun intersectionPoint(ray: Ray, plane: Plane): Point {
        val rayToPlaneVector = vectorBetween(ray.point, plane.point)
        val scaleFactor =
            rayToPlaneVector.dotProduct(plane.normal) / ray.vector.dotProduct(plane.normal)
        return ray.point.translate(ray.vector.scale(scaleFactor))
    }
}
