# Design Rationale: Route Planner (Assignment 3, Bridge + Adapter)

## 1. Problem and domain

A routing service receives an origin, a destination and a planning style (`fastest`, `shortest`, `scenic`) and returns one route. Two things vary independently:

- **How a route is chosen** from the candidates (the abstraction axis): `FastestRoutePlanner`, `ShortestRoutePlanner`, `ScenicRoutePlanner`. `ScenicRoutePlanner` also applies a detour limit, so the refinements carry real behaviour.
- **Where the candidates come from** (the implementor axis): `AtlasMapProvider` (worldwide), `AlpineMapProvider` (Europe only) and `LegacyGeoRouterAdapter` (an old geocoder covering Central Asia).

The abstraction (`RoutePlanner`) holds a `MapProvider` and never sees anything else. The `plan` method is final; refinements only override `select` and `strategyName`.

**Chosen complexity module: dynamic implementor selection.** Nothing in the client names a provider. Every `MapProvider` reports its own `coverage()` region. `ProviderSelector` keeps the providers whose coverage contains both points of the request and orders them by coverage area, most specialised first. `RoutingService` builds the planner from the style string through a registry of factories, and tries the next candidate when one throws `ProviderUnavailableException`. Astana to Almaty is served by the legacy provider, Paris to Lyon by Alpine, Tokyo to Osaka by Atlas. If the legacy link is down, the same request silently falls back to Atlas.

## 2. Why one pattern is not enough

**Bridge alone.** Bridge gives the structure (3 styles x 3 providers = 6 classes instead of 9 combined subclasses), but it needs an `Implementor` that the legacy class cannot be. `LegacyGeoRouter` is foreign code we may not modify, and it does not implement `MapProvider`. Without a translation layer, the abstraction would have to know the legacy class, its status codes and its exceptions, which breaks the rule that the abstraction depends only on the Implementor interface.

**Adapter alone.** An adapter would make the legacy class look like `MapProvider`, but it says nothing about how to combine three selection styles with several providers. Without Bridge, each style would be duplicated per provider (`FastestAtlas`, `ScenicLegacy`, and so on), and every new provider or style would multiply the classes.

Bridge decides *where the seam is*; Adapter makes the awkward class *fit into that seam*.

## 3. Why the wrapped class is genuinely incompatible

| Aspect | `MapProvider` contract | `LegacyGeoRouter` |
|---|---|---|
| Operation | `findRoutes(Coordinate, Coordinate)` returns `List<RouteOption>` | `solve(double[], double[], LegacyRouteSet)` returns `int` |
| Argument order | latitude, longitude | arrays are `[longitude, latitude]` |
| Result delivery | return value | out-parameter filled as a side effect |
| Units and types | km, `Duration`, `List<Coordinate>`, scenic score 0..100 | miles, whole minutes, string `"lon,lat;lon,lat"`, grade `A..E` |
| Failure mechanism | checked `RoutingException` hierarchy | status codes 17 / 22 / 503 plus an unchecked `LegacyLinkException` |
| Coverage | declared by the provider | unknown, supplied to the adapter by configuration |

## 4. Failure translation

The adapter converts every legacy failure into the contract's exception hierarchy. It attaches no cause and uses no legacy vocabulary in messages, so nothing adapted-specific leaks.

| Legacy outcome | Contract exception |
|---|---|
| status 17 (no road) | `RouteNotFoundException` |
| status 22 (bad coordinates) | `InvalidLocationException` |
| status 503 (busy), unknown status | `ProviderUnavailableException` |
| `LegacyLinkException` or any other runtime failure | `ProviderUnavailableException` |
| OK status but zero routes | `RouteNotFoundException` |
| malformed waypoints, unknown grade, negative values | `ProviderUnavailableException` |

Because the failures are consistent, `RoutingService` handles them uniformly: only `ProviderUnavailableException` triggers a fallback, regardless of which provider raised it.

## 5. Open/Closed on both axes

- **New abstraction variant:** subclass `RoutePlanner` and register one factory. No existing class changes (test `newRefinedAbstractionNeedsNoChangesToExistingClasses`).
- **New implementor:** implement `MapProvider` (or write another adapter) and add it to the provider list (test `openForExtensionOnBothAxes`).

Registration happens in the composition root (`RoutingConfig`), which is wiring code, not a domain class.

## 6. Limitation

The `MapProvider` contract is a lowest common denominator. The scenic score has to exist for every provider, so the legacy letter grade is mapped onto five fixed values (A=100 ... E=20). This mapping is lossy and arbitrary, and scores from different providers are not truly comparable. Richer provider data (traffic, tolls, turn-by-turn steps) cannot be exposed without widening the contract.
