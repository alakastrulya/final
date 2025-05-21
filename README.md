# Battle Tank Game (LibGDX Java)
This is a modern implementation of the classic Battle City game built using Java and the LibGDX framework. The project demonstrates the use of multiple design patterns (State, Strategy, Command, Factory, Observer), supports both single-player and two-player modes, includes destructible bases, and follows SOLID principles for maintainable code.

## Features
- Single-player and two-player modes

- Destructible base (eagle) and walls

- Shooting, explosions, and collision mechanics

- Main game loop with state transitions

- Enemy AI using different behaviors (chase, attack base, wander)

- Clean, modular code following SOLID principles

- Animations, intro curtains, pixel fonts, and custom UI

## Technologies Used
- Java 17 or higher

- LibGDX

- Gradle

- IntelliJ IDEA or Android Studio

## Project Structure
- assets/ — sprites, fonts, maps, and sounds
- com.mg.game/ package contains:

- tank/ — tank logic and state handling (State pattern)

- bullet/ — bullet creation and management (Factory)

- map/ — map tiles and base handling

- explosion/ — explosion animations

- level/ — intro and level completion screens

- command/ — input handling using Command pattern

- observer/ — event system for base destruction

- strategy/ — AI logic (Strategy pattern)

- manager/ — collision detection and enemy spawning

- menu/ — main menu logic

- GameScreen.java — the main game screen

## Design Patterns Used
- State — for tank movement state (moving up/down/left/right, idle)
- Strategy — for enemy behavior (aggressive, attack base, wander)
- Command — encapsulates player input actions as objects
- Factory — creates tanks, bullets, explosions based on parameters
- Observer — notifies when the base is destroyed, passes game context

## How to Run
Clone the repository:
git clone https://github.com/your-username/battle-tank-game.git
cd battle-tank-game

Run via Gradle:
./gradlew desktop:run

## Controls
Player 1:

- Move: Arrow keys

- Shoot: Enter

- Player 2:

- Move: W, A, S, D

- Shoot: Space

## Future Plans

Save and load progress

Bonus items and power-ups

More enemy types and maps

## Authors - 2nd years students of Narxoz University
- Abdrakhmanova Adelya
- Akhmetova Aisha
- Gabdyrafik Ravil

Thank you for your attention! 
