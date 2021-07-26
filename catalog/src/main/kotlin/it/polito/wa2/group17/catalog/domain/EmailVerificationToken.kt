package it.polito.wa2.group17.catalog.domain

import com.sun.istack.NotNull
import it.polito.wa2.group17.common.utils.AbstractEntity
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import java.time.Instant
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class EmailVerificationToken(
    @NotNull
    val username: String,
    @NotNull
    val expireDate: Instant
) : AbstractEntity<UUID>() {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    private var id: UUID? = null
    override fun getId(): UUID? = id

}
