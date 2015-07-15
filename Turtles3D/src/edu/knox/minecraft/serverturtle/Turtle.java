package edu.knox.minecraft.serverturtle;

import java.util.ArrayList;

import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Direction;
import net.canarymod.api.world.position.Position;
import net.canarymod.chat.MessageReceiver;

public class Turtle {

    //POSITION VARIABLES

    //in world position of player at turtle on --> (0,0,0) for Turtle's relative coord system.
    private Position originPos;

    //current relative position
    private Position relPos;

    //true current position/direction in game coords(made by combining relative and real)
    private Position gamePos;
    private Direction dir;

    //OTHER VARIABLES
    private boolean bp = false;  //Block Place on/off
    private BlockType bt = BlockType.Stone;  //default turtle block type 
    private World world;  //World in which all actions occur
    private MessageReceiver sender;  //player to send messages to
    private ArrayList<BlockRecord> oldBlocks;  //original pos/type of all bricks laid by this turtle for undoing

    ///////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize the turtle.  Called when executing a script (or turning command line turtle on) 
     * @param sender
     */
    public void turtleInit(MessageReceiver sender)  {
        //save sender for later
        this.sender = sender;

        //GET WORLD
        world = sender.asPlayer().getWorld();

        //Set up positions

        //Get origin Position and Direction
        originPos = sender.asPlayer().getPosition();
        dir = sender.asPlayer().getCardinalDirection();

        //Make the Relative Position
        relPos = new Position(0,0,0);

        //Update game position
        gamePos = new Position(); 
        updateGamePos();
    }       

    /**
     * Output a message to the player console.
     * 
     * @param sender
     * @param args
     */
    public void turtleConsole(String msg)
    {
        sender.message(msg); 
    }  

    /**
     * Toggle block placement mode on/off.
     * 
     * TODO:  IF placement off -> dont change vs AIr placement?
     * @param sender
     * @param args
     */
    public void turtleToggleBlockPlace()
    {
        bp = !bp;
        turtleBlockPlaceStatus();  //alert user about change
    }

    /**
     * Reports whether block placement mode is on.
     * @param sender
     * @param args
     */
    public void turtleBlockPlaceStatus()
    {
        if(bp)  {
            turtleConsole("Block placement mode on.");
        }  else {
            turtleConsole("Block placement mode off.");
        }
    }

    /**
     * Set turtle position (relative coords)
     * @param sender
     * @param args
     */
    public void turtleSetRelPosition(int x, int y, int z)
    {     
        relPos = new Position(x, y, z);
    }

    /**
     * Set turtle direction.  Number based.
     * 
     * @param sender
     * @param args
     */
    public void turtleSetDirection(int dir)
    {
        //update direction
        // 0 = NORTH
        // 1 = NORTHEAST
        // 2 = EAST
        // 3 = SOUTHEAST
        // 4 = SOUTH
        // 5 = SOUTHWEST
        // 6 = WEST
        // 7 = NORTHWEST
        // Else = ERROR 
        this.dir = (Direction.getFromIntValue(dir));
    }

    /**
     * Report current position (relative)
     * @param sender
     * @param args
     */
    public void turtleReportPosition()
    {
        turtleConsole("" + relPos);
    }

    /**
     * Report current position of Turtle in game coords
     * @param sender
     * @param args
     */
    public void turtleReportGamePosition()
    {
        turtleConsole("" + gamePos);
    }

    /**
     * Report position of relative origin (Player's pos at Turtle on) in game coords
     * @param sender
     * @param args
     */
    public void turtleReportOriginPosition()
    {
        turtleConsole("" + originPos);
    }

    /**
     * Report current direction (relative)
     * @param sender
     * @param args
     */
    public void turtleReportDirection()
    {
        turtleConsole("" + dir);
    }

    /**
     * Set block type (int based)
     * @param sender
     * @param args
     */
    public void turtleSetBlockType(int blockType)
    {
        if (!bp)  //don't allow if block placement mode isn't on
            return;

        bt = BlockType.fromId(blockType);      
    }

    /**
     * set Block type (string/BlockType based)
     * @param sender
     * @param args
     */
    //TODO implementation-- maybe we don't need this version?

    /**
     * Report current block type
     * @param sender
     * @param args
     */
    public void turtleReportBlockType()
    {
        if (!bp)  //don't allow if block placement mode isn't on
            return;

        //report current BT of turtle   
        turtleConsole("" + bt);
    }

    /**
     * Move (forward/back)
     * 
     * @param sender
     * @param args
     */
    public void turtleMove(int dist)
    {
        boolean fd = false;  //flipped direction (for moving backward) 

        //check if distance is negative (going backward)
        if (dist < 0){  
            //if so, reverse turtle direction
            dist = Math.abs(dist);
            flipDir();
            fd = true;
        }

        for (int i = dist; i > 0; i--){

            //update turtle position
            relPos = calculateMove(relPos, dir, false, false);
            updateGamePos();

            //Place block if block placement mode on
            if (bp) {
              //TODO:  keep track of this block to undo
                //oldBlocks.add(getBlockAt)
                //oops... need to look up and see if this is a real method.
                
                world.setBlockAt(gamePos, bt);                 
                
            }
        }

        //if reversed turtle direction, reset to original
        if (fd == true){
            flipDir();
        }
    }

    /**
     * Moves turtle up/down
     * @param sender
     * @param args
     */
    public void turtleUpDown(int dist)
    {
        boolean up = true;  //default direction is up

        //check if distance is negative (going down)
        if (dist < 0 ){  
            //if so, reverse turtle direction
            dist = Math.abs(dist);
            up = false;
        }

        for (int i = dist; i > 0; i--){

            //update turtle position
            relPos = calculateMove(relPos, dir, up, !up); 
            updateGamePos();

            //Place block if block placement mode on
            if (bp) {
                world.setBlockAt(gamePos, bt);    
                //TODO:  keep track of this block to undo
            }
        }
    }

    /**
     * Turn right/left.
     * 
     * @param sender
     * @param args
     */ 
    public void turtleTurn(boolean left, int deg)
    {
        //turn turtle
        dir = calculateTurn(dir, left, deg);
    }

    /**
     * Return whether block placement mode is on.  Called by TurtleTester.
     */
    public boolean getBP()  {
        return bp;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //PRIVATE HELPER FUNCTIONS

    /**
     * Update game pos
     */
    private void updateGamePos() {
        //get origin coords
        int xo = originPos.getBlockX();
        int yo = originPos.getBlockY();
        int zo = originPos.getBlockZ();     

        //get relative coords
        int xr = relPos.getBlockX();
        int yr = relPos.getBlockY();
        int zr = relPos.getBlockZ();

        //update game position
        //gamePos = originPos + relPos;
        gamePos.setX(xo+xr);
        gamePos.setY(yo+yr);
        gamePos.setZ(zo+zr);
    }

    /**
     * Reverses relative direction (turn 180 degrees).  Used when moving backward.
     */
    private void flipDir(){
        //get current direction (N, NE, ... , S --> 0, 1, ... , 7)
        int dirInt = dir.getIntValue();  

        //calculate new direction
        dirInt = (dirInt + 4) % 8;

        //update relDir
        dir = Direction.getFromIntValue(dirInt);
    }

    /**
     * Calculate move.  Returns new relative position of Turtle.
     * 
     * @param p Initial relative position of turtle
     * @param d forward direction of turtle
     * @param up Is this move going up?
     * @param down Is this move going down?
     * @return New relative position of turtle.
     */
    private Position calculateMove (Position p, Direction d, boolean up, boolean down){ 

        int dn = d.getIntValue();  //get direction number

        //check if vertical motion
        if (up || down ){
            if (up)  {  //moving up
                //add y +1
                p.setY(p.getBlockY() + 1);

            }else  {  //otherwise moving down
                //subtract y -1
                p.setY(p.getBlockY() - 1);
            }

        }  else  {  //2D motion
            if(dn == 0){ //NORTH
                //subtract z -1
                p.setZ(p.getBlockZ() - 1);

            }else if(dn == 1){//NORTHEAST
                //subtract z -1
                //add x +1
                p.setZ(p.getBlockZ() - 1);
                p.setX(p.getBlockX() + 1);

            }else if(dn == 2){//EAST
                //add x +1
                p.setX(p.getBlockX() + 1);

            }else if(dn == 3){//SOUTHEAST
                //add z +1
                //add x +1
                p.setZ(p.getBlockZ() + 1);
                p.setX(p.getBlockX() + 1);

            }else if(dn == 4){//SOUTH
                //add z +1
                p.setZ(p.getBlockZ() + 1);

            }else if(dn == 5){//SOUTHWEST
                //add z +1
                //subtract x -1
                p.setZ(p.getBlockZ() + 1);
                p.setX(p.getBlockX() - 1);

            }else if(dn == 6){//WEST
                //subtract x -1
                p.setX(p.getBlockX() - 1);

            }else if(dn == 7){//NORTHWEST
                //subtract z -1
                //subtract x -1
                p.setZ(p.getBlockZ() - 1);
                p.setX(p.getBlockX() - 1);

            }else {
                //BAD STUFF
                //Not one of the 8 main directions.  
                //Will require more math, but maybe we don't want to worry about this case.
            }
        }
        return p;  //return updated position
    }

    /**
     *  Calculate turn.  Returns new relative direction of Turtle.
     *  
     * @param d  Initial relative direction of turtle.
     * @param left Is this turn going left?  (False -> turning right)
     * @param deg  number of degrees to turn in specified direction
     * @return New relative direction of turtle.
     */
    private Direction calculateTurn(Direction d, boolean left, int deg)  {

        //get current direction (N, NE, ... , S --> 0, 1, ... , 7)
        int dirInt = d.getIntValue();  

        //calculate new direction            
        //This currently only works correctly for 45 deg intervals.  It may be okay to leave it that way.

        int turns = deg/45;  //desired number of eighth turns

        if (left)  {  //turning left
            dirInt -= turns;
        }  else  {  //turning right
            dirInt += turns;
        }

        dirInt = dirInt % 8;

        //update direction and return
        d = Direction.getFromIntValue(dirInt);
        return d;
    }
}