import kotlinx.cinterop.*
import platform.posix.exit
import sdl2.*

fun main() {
    if (SDL_Init(SDL_INIT_VIDEO) != 0) {
        println("Error initializing SDL: ${SDL_GetError()?.toKString()}")
        exit(1)
    }

    val window = SDL_CreateWindow(
        "Kotlin Native SDL Demo",
        SDL_WINDOWPOS_CENTERED.toInt(),
        SDL_WINDOWPOS_CENTERED.toInt(),
        800,
        600,
        SDL_WINDOW_SHOWN
    )

    if (window == null) {
        println("Error creating window: ${SDL_GetError()?.toKString()}")
        SDL_Quit()
        exit(1)
    }

    val renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED)
    if (renderer == null) {
        println("Error creating renderer: ${SDL_GetError()?.toKString()}")
        SDL_DestroyWindow(window)
        SDL_Quit()
        exit(1)
    }

    val imagePath = "images/pokeball.bmp"
    val surface: CPointer<SDL_Surface>? = SDL_LoadBMP(imagePath)
    if (surface == null) {
        println("Error loading image: ${SDL_GetError()?.toKString()}")
        SDL_DestroyRenderer(renderer)
        SDL_DestroyWindow(window)
        SDL_Quit()
        exit(1)
    }

    val texture = SDL_CreateTextureFromSurface(renderer, surface)
    if (texture == null) {
        println("Error creating texture: ${SDL_GetError()?.toKString()}")
        SDL_FreeSurface(surface)
        SDL_DestroyRenderer(renderer)
        SDL_DestroyWindow(window)
        SDL_Quit()
        exit(1)
    }

    SDL_FreeSurface(surface)

    var running = true
    memScoped {
        val event = alloc<SDL_Event>()
        while (running) {
            while (SDL_PollEvent(event.ptr) != 0) {
                if (event.type == SDL_QUIT) {
                    running = false
                }
            }

            // clear screen
            SDL_SetRenderDrawColor(renderer, 0u, 0u, 0u, 255u)
            SDL_RenderClear(renderer)

            SDL_RenderCopy(renderer, texture, null, null) // draw texture
            SDL_RenderPresent(renderer) // render screen
        }
    }

    SDL_DestroyTexture(texture)
    SDL_DestroyRenderer(renderer)
    SDL_DestroyWindow(window)
    SDL_Quit()
}

// TODO where did this wrapper go? I recall this being in SDL before...
fun SDL_LoadBMP(filePath: String): CPointer<SDL_Surface>? {
    memScoped {
        val file = SDL_RWFromFile(filePath, "rb") ?: return null
        val surface = SDL_LoadBMP_RW(file, 1) // 1 means close the RW file after reading
        return surface
    }
}