package it.polito.wa2.group17.catalog.security.jwt

import it.polito.wa2.group17.catalog.service.UserDetailsServiceExtendedImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.jvm.Throws

class JwtAuthenticationTokenFilter : OncePerRequestFilter() {

    @Autowired
    private lateinit var jwtUtils: JwtUtils

    @Autowired
    private lateinit var userDetailsService : UserDetailsServiceExtendedImpl

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try{
            var jwt = parseJwt(request)
            if(jwt != null && jwtUtils.validateJwtToken(jwt)){

                val username = jwtUtils.getDetailsFromJwtToken(jwt).username
                val userDetails = userDetailsService.loadUserByUsername(username)
                val authentication = UsernamePasswordAuthenticationToken(userDetails,null,userDetails.authorities)

                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        }catch (e: Exception) {
            logger.error("User authentication failure: {}", e)
        }

        filterChain.doFilter(request,response)
    }

    private fun parseJwt(request: HttpServletRequest): String? {
        var header = request.getHeader("Authorization")
        val bearer = "Bearer "

        header = if(StringUtils.hasText(header) && header.startsWith(bearer))
            header.substring(bearer.length,header.length)
        else
            null

        return header
    }
}
