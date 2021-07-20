package it.polito.wa2.group17.common.utils

import org.springframework.data.util.ProxyUtils
import java.io.Serializable
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
class BaseEntity<ID : Serializable> : AbstractEntity<ID>() {
    @Id
    @GeneratedValue
    private var id: ID? = null
    override fun getId(): ID? = id
}

abstract class AbstractEntity<ID : Serializable> {

    companion object {
        private const val serialVersionUID = -43869754L
    }

    abstract fun getId(): ID?

    override fun toString(): String = "@Entity ${this.javaClass.name}(id=${getId()})"

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other === this) return true
        if (javaClass != ProxyUtils.getUserClass(other)) return false
        other as BaseEntity<*>
        return if (null == getId()) false else this.getId() == other.getId()
    }

    override fun hashCode(): Int {
        return 31
    }
}
