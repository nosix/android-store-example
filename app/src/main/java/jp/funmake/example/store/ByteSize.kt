package jp.funmake.example.store

val Long.kB
    get() = this * 1024

val Long.MB
    get() = this.kB * 1024

val Long.GB
    get() = this.MB * 1024

val Int.MB
    get() = this.toLong().MB

val Int.GB
    get() = this.toLong().GB

val Long.withUnit
    get() = ByteSize.from(this)

data class ByteSize(val value: Long, val unit: Unit) {

    override fun toString(): String = "$value$unit"

    enum class Unit {
        byte, kB, MB, GB
    }

    companion object {
        tailrec fun from(value: Long, unitIndex: Int = 0): ByteSize {
            check(unitIndex < Unit.values().size)
            if (value < 1024) return ByteSize(value, Unit.values()[unitIndex])
            return from(value / 1024, unitIndex + 1)
        }
    }
}