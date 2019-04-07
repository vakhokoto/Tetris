package tetris;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class JBrainTetris extends JTetris{
    private DefaultBrain brain;
    private JCheckBox brainMode;
    private JComponent panel;
    private JSlider adversary;
    private Random r;

    public JBrainTetris(int pixels){
        super(pixels);
        r = new Random();
        r.nextInt(100);
        brain = new DefaultBrain();
    }


    /**
     * this method makes moves regarding to DefaultBRain instructions
     * and uses super's tick for actual move
     * */
    @Override
    public void tick(int verb){
        if (verb == DOWN && brainMode.isSelected()){
            board.undo();
            Brain.Move move = brain.bestMove(board, currentPiece, HEIGHT, null);
            if (move != null) {
                TPoint[] b1 = currentPiece.getBody();
                TPoint[] b2 = move.piece.getBody();
                if (!currentPiece.equals(move.piece)) {
                    super.tick(ROTATE);
                }
                if (currentX < move.x) {
                    super.tick(RIGHT);
                } else if (currentX > move.x){
                    super.tick(LEFT);
                }
            }
            super.tick(verb);
        } else {
            super.tick(verb);
        }
    }

    private static final int SLIDER_MAX_VALUE = 100;
    private static final int SLIDER_HEIGHT = 15;
    private static final int SLIDER_WIDTH = 100;

    /**
     * creates control panel for new JBrainTetris what is dowes is
     * calls super's CreateControlPanel and thana add it's own part
     * */
    @Override
    public JComponent createControlPanel(){
        panel = super.createControlPanel();
        panel.add(new JLabel("Brain:"));
        brainMode = new JCheckBox("Brain active");
        panel.add(brainMode);
        panel.add(new JLabel("Adversary:"));
        adversary = new JSlider(0, SLIDER_MAX_VALUE, 0);
        adversary.setPreferredSize(new Dimension(SLIDER_WIDTH,SLIDER_HEIGHT));
        panel.add(adversary);
        return panel;
    }


    /**
     * picks the next piece simillar to super's pickNextPiece
     * */
    @Override
    public Piece pickNextPiece(){
        int max = adversary.getValue();
        int rNum = r.nextInt(SLIDER_MAX_VALUE - 1) + 1;
        System.out.println(max + " " + rNum);
        if (rNum <= max){
            return getNextPiece();
        } else {
            return super.pickNextPiece();
        }
    }


    /**
     * finds the worst piece for current scenario
     * */
    private Piece getNextPiece() {
        Brain.Move move = null;
        for (int i=0; i<pieces.length; ++i){
            Piece curPiece = pieces[i];
            Brain.Move curMove = brain.bestMove(board, curPiece, HEIGHT, null);
            if (move == null || (curMove != null && move.score < curMove.score)){
                move = curMove;
            }
            curPiece = curPiece.fastRotation();
            while (!curPiece.equals(pieces[i])){
                curMove = brain.bestMove(board, curPiece, HEIGHT, null);
                if (move == null || (curMove != null && move.score < curMove.score)){
                    move = curMove;
                }
                curPiece = curPiece.fastRotation();
            }
        }
        return (move != null ?move.piece:null);
    }

    public static void main(String[] atgs) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        JBrainTetris tetris = new JBrainTetris(16);
        JFrame frame = JTetris.createFrame(tetris);
        frame.setVisible(true);
    }
}
