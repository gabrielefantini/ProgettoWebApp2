package it.polito.wa2.group17.catalog.security

import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.catalog.exceptions.security.UserNotAllowedException
import it.polito.wa2.group17.catalog.exceptions.security.UserNotAuthenticatedException
import it.polito.wa2.group17.catalog.service.UserDetailsServiceExtended
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

annotation class OnlyEnabledUsers

@Aspect
@Component
class OnlyEnabledUsersAspect {

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(OnlyEnabledUsersAspect::class.java)
    }

    @Autowired
    private lateinit var userDetailsServiceExtended: UserDetailsServiceExtended

    @Around("@annotation(OnlyEnabledUsers)")
    fun filterEnabledUsersAccess(proceedingJoinPoint: ProceedingJoinPoint): Any {
        logger.info("Checking if the user is enabled to perform the operation {}", proceedingJoinPoint.signature.name)
        val authentication = SecurityContextHolder.getContext().authentication

        if (!authentication.isAuthenticated)
            throw UserNotAuthenticatedException()
        if (authentication.principal !is UserDetailsDto)
            throw IllegalStateException("Unexpected principal ${authentication.principal}")

        val user = authentication.principal as UserDetailsDto
        val actualUser = userDetailsServiceExtended.loadUserByUsername(user.username)

        if (!actualUser.isEnabled)
            throw UserNotAllowedException(actualUser.username)

        logger.info(
            "User {} is enabled and can perform the operation {}",
            actualUser.username,
            proceedingJoinPoint.signature.name
        )
        return proceedingJoinPoint.proceed()
    }
}
