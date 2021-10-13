package it.polito.wa2.group17.catalog.security

import it.polito.wa2.group17.catalog.connector.LoginConnector
import it.polito.wa2.group17.catalog.connector.LoginConnectorMocked
import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.catalog.exceptions.security.UserNotAdminException
import it.polito.wa2.group17.catalog.exceptions.security.UserNotAllowedException
import it.polito.wa2.group17.catalog.exceptions.security.UserNotAuthenticatedException
import it.polito.wa2.group17.catalog.service.SignInAndUserInfo
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

annotation class OnlyAdmins

@Aspect
@Component
class OnlyAdminsAspect {

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(OnlyEnabledUsersAspect::class.java)
    }

    @Autowired
    private lateinit var signInAndUserInfo: SignInAndUserInfo

    @Around("@annotation(OnlyAdmins)")
    fun filterEnabledUsersAccess(proceedingJoinPoint: ProceedingJoinPoint): Any {
        logger.info("Checking if the user is an admin to perform the operation {}", proceedingJoinPoint.signature.name)
        val authentication = SecurityContextHolder.getContext().authentication

        if (!authentication.isAuthenticated)
            throw UserNotAuthenticatedException()
        if (authentication.principal !is UserDetailsDto)
            throw IllegalStateException("Unexpected principal ${authentication.principal}")

        val user = authentication.principal as UserDetailsDto
        logger.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA ${user.username}")
        val actualUser = signInAndUserInfo.findUser(user.username)

        if (actualUser != null) {
            logger.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA ${actualUser.username}")
        }

        if (!actualUser!!.isEnabled)
            throw UserNotAllowedException(actualUser.username)

        if (!actualUser.roles.contains(RoleName.ADMIN))
            throw UserNotAdminException(actualUser.username)


        logger.info(
            "User {} is enabled and can perform the operation {}",
            actualUser.username,
            proceedingJoinPoint.signature.name
        )
        return proceedingJoinPoint.proceed()
    }
}
