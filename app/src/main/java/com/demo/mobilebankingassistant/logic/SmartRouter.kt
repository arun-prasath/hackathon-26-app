package com.demo.mobilebankingassistant.logic

import com.demo.mobilebankingassistant.llm.LocalLlmRoute
import com.demo.mobilebankingassistant.util.AppLogger

enum class RouteType {
    LOCAL, BACKEND
}

data class RoutingDecision(
    val route: RouteType,
    val reasoning: String,
    val tokenSavings: Int,
    val localAnswer: String? = null
)

class SmartIntentRouter {
    private val trivialIntents = listOf(
        "balance", "statement", "profile", "logout", "login", 
        "hello", "hi", "help", "settings", "fixed deposit", "mutual fund",
        "home loan", "personal loan", "car loan", "interest rate", "product",
        "offer", "branch", "atm"
    )

    private val backendOnlyIntents = listOf(
        "transfer", "pay ", "send money", "pin change", "change pin", "password",
        "otp", "fraud", "lost card", "stolen", "complaint", "loan rejected",
        "credit decision", "update mobile", "change address"
    )

    fun route(query: String, localLlmRoute: LocalLlmRoute? = null): RoutingDecision {
        val lowerQuery = query.lowercase()
        val mustUseBackend = backendOnlyIntents.any { lowerQuery.contains(it) }
        val isTrivial = trivialIntents.any { lowerQuery.contains(it) }
        AppLogger.i(
            "Router",
            "Routing query. queryLength=${query.length}, llmRoute=${localLlmRoute?.parsedRoute}, mustUseBackend=$mustUseBackend, isTrivial=$isTrivial"
        )

        return if (mustUseBackend) {
            AppLogger.i("Router", "Decision=BACKEND due to policy override")
            RoutingDecision(
                route = RouteType.BACKEND,
                reasoning = "Banking policy override: this is sensitive or transactional, so it must route to SCB AI Factory/backend even if a local model is available.",
                tokenSavings = 0
            )
        } else if (localLlmRoute != null && localLlmRoute.parsedRoute != "UNPARSED") {
            if (localLlmRoute.local) {
                AppLogger.i("Router", "Decision=LOCAL from LiteRT-LM")
                RoutingDecision(
                    route = RouteType.LOCAL,
                reasoning = "LiteRT-LM classified this as safe for local handling. Parsed route: ${localLlmRoute.parsedRoute}. Model reason: ${localLlmRoute.reason}",
                    tokenSavings = (query.length / 4) + 35,
                    localAnswer = localLlmRoute.answer
                )
            } else {
                AppLogger.i("Router", "Decision=BACKEND from LiteRT-LM")
                RoutingDecision(
                    route = RouteType.BACKEND,
                    reasoning = "LiteRT-LM classified this for backend handling. Parsed route: ${localLlmRoute.parsedRoute}. Model reason: ${localLlmRoute.reason}",
                    tokenSavings = 0
                )
            }
        } else if (localLlmRoute != null && isTrivial) {
            AppLogger.i("Router", "Decision=LOCAL from rules after unparsed LiteRT-LM response")
            RoutingDecision(
                route = RouteType.LOCAL,
                reasoning = "LiteRT-LM responded but did not return parseable route JSON, so rules safely classified this trivial intent as local. Raw model response is visible in the debug panel.",
                tokenSavings = (query.length / 4) + 10
            )
        } else if (localLlmRoute != null) {
            AppLogger.i("Router", "Decision=BACKEND after unparsed LiteRT-LM response")
            RoutingDecision(
                route = RouteType.BACKEND,
                reasoning = "LiteRT-LM responded but did not return parseable route JSON. Routing to backend because the intent is not safely trivial. Raw model response is visible in the debug panel.",
                tokenSavings = 0
            )
        } else if (isTrivial) {
            AppLogger.i("Router", "Decision=LOCAL from rules-only fallback")
            RoutingDecision(
                route = RouteType.LOCAL,
                reasoning = "Rules-only fallback: trivial intent detected and handled on device. Add a .litertlm model to enable LiteRT-LM classification.",
                tokenSavings = (query.length / 4) + 10 // Mock calculation
            )
        } else {
            AppLogger.i("Router", "Decision=BACKEND from rules-only fallback")
            RoutingDecision(
                route = RouteType.BACKEND,
                reasoning = "Rules-only fallback: complex or uncertain query requires larger reasoning capabilities. Routing to SCB AI Factory.",
                tokenSavings = 0
            )
        }
    }
}
