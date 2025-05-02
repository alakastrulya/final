public class GameScreen {
    private int playerCount;
    public GameScreen(int playerCount) {
        this.playerCount = playerCount;
    }

    @Override
    public void pause(){
        System.out.println("Game Paused");
    }

    @Override
    public void resume(){
        System.out.println("Game resumed");
    }

    @Override
    public void show(){
        System.out.println("Player count: " + playerCount);
    }

    @Override
    public void hide(){
        System.out.println("Hiding tanks");
    }

    @Override
    public void dispose(){
        System.out.println("Game disposed");
    }

    @Override
    public void resize(int width, int height){
        System.out.println("Game resized");
    }
}
