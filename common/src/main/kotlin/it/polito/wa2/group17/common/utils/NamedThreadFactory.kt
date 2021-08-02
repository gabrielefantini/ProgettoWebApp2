package it.polito.wa2.group17.common.utils

import java.util.concurrent.ThreadFactory

class NamedThreadFactory(val namer: (Runnable) -> String) : ThreadFactory {
    constructor(name: String) : this({ name })

    override fun newThread(r: Runnable) = Thread(r, namer(r))
}
