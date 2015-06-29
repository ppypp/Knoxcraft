package edu.knox.minecraft.plugintest;

import net.canarymod.Canary;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Direction;
import net.canarymod.api.world.position.Position;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.CommandListener;
import net.canarymod.hook.HookHandler;
import net.canarymod.logger.Logman;
import net.canarymod.plugin.Plugin;
import net.canarymod.plugin.PluginListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.knoxcraft.hooks.UploadJSONHook;
import edu.knoxcraft.http.server.HttpUploadServer;

// If tt is not true-> no other command works??
//Or we could keep rel pos/dir because they get set up intially (or should be)

//Things need to be overly simple during testing for ease of use

//In time, need to build in string verification for correct input style (ie. All caps, etc)


public class TurtleAPI extends Plugin implements CommandListener, PluginListener {

	Turtle turtle;
	BlockType bt;
	//World in which all actions occur
	private World world;
	//in world position
	private Position truePos;
	private Direction trueDir;
	//relative position
	private Position relPos;
	private Direction relDir;
	//current position (made by combining relative and real) -> this gets sent to the game 
	//By subtracting truePos from curPos, you can get relPos -> MaKE helper methodd to handle this!!!!
	private Position curPos;
	private Direction curDir;
	private HttpUploadServer httpServer;
	
	public static Logman logger;
	
	//Turtle on/off
	private boolean tt;
	//Block Place on/off
	private boolean bp;
	
	public TurtleAPI() {
	    TurtleAPI.logger = getLogman();
	}
	
	///***************???????????/
	//OLD CODE
//	 @Command(
//	            aliases = { "turtleoff", "toff" },
//	            description = "Turns off turtle mode",
//	            permissions = { "" },
//	            toolTip = "/turtleoff")
//	    public void turtleOffCommand(MessageReceiver sender, String[] args)
//	    {
//	    	turtleMode = false;
//	    	turtle = null;
//	    	
//	    	//alert player
//	    	sender.message("Turtle mode off");
//	    }
//	    
//	    @Command(
//	            aliases = { "forward"},
//	            description = "Move the turtle forward dropping blocks",
//	            permissions = { "" },
//	            toolTip = "/forward [spaces]")
//	    public void turtleForwardCommand(MessageReceiver sender, String[] args)
//	    {
//	    	if (turtleMode)  {
//	    		//did the user specify a number of spaces?
//	    		int spaces = 1;   		
//	    		if (args.length > 1)  {
//	    			spaces = Integer.parseInt(args[1]);
//	    		}
//	    		
//	    		//move forward the desired number of spaces
//	    		Vector3D forDir = sender.asPlayer().getForwardVector();
//	    		for (int i=0; i<spaces; i++)  {
//	    			turtle.forward(forDir);
//	    		}
//	    		
//	    	}  else {
//	    		//alert player
//	        	sender.message("Turtle mode is not on.");
//	    	}
	//END OF OLD CODE
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//HELPER FUNCTIONS
	private void updateCurPos(){
		int xt = truePos.getBlockX();
		int yt = truePos.getBlockY();
		int zt = truePos.getBlockZ();		
		
		int xr = relPos.getBlockX();
		int yr = relPos.getBlockY();
		int zr = relPos.getBlockZ();
		
		//curPos = truePos + relPos;
		
		curPos.setX(xt+xr);
		curPos.setY(yt+yr);
		curPos.setZ(zt+zr);
		Canary.hooks().callHook(null);
	}
	
	private void updateCurDir(){
		
	}
	private void getRelPos(){
		int xc = curPos.getBlockX();
		int yc = curPos.getBlockY();
		int zc = curPos.getBlockZ();
		
		int xt = truePos.getBlockX();
		int yt = truePos.getBlockY();
		int zt = truePos.getBlockZ();	
		
		relPos.setX(xc-xt);
		relPos.setY(yc-yt);
		relPos.setZ(zc-zt);
		
	}
	
	
	
	private void getString(MessageReceiver sender, boolean b){
		//Get the Boolean value 
		String [] str = new String [2];
		str[0] = "/c";
		str[1] = b + "";
				
		//return status of BP using TurtleConsole
		TurtleConsole(sender, str);
	}
	
	private void getString(MessageReceiver sender, BlockType b){
		//Get the Boolean value 
		String []str = new String [2];
		str[0] = "/c";
		str[1] = b.toString() + "";
				
		//return status of BP using TurtleConsole
		TurtleConsole(sender, str);
	}
	
	private void getString(MessageReceiver sender, Direction b){
		//Get the Boolean value 
		String [] str = new String [2];
		str[0] = "/c";
		str[1] = b.toString() + "";
				
		//return status of BP using TurtleConsole
		TurtleConsole(sender, str);
	}
	
	private void getString(MessageReceiver sender, Position b){
		//Get the Boolean value 
		String [] str = new String [2];
		str[0] = "/c";
		str[1] = b.toString() + ""; //Need to overload / fix this output
				
		//return status of BP using TurtleConsole
		TurtleConsole(sender, str);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void disable() {
	    httpServer.disable();
	}

	@Override
	public boolean enable() {
		try {
		    getLogman().info("Registering plugin");
			Canary.hooks().registerListener(this, this);
			httpServer=new HttpUploadServer();
			httpServer.enable();
			//getName() returns the class name, in this case TurtleAPI
			getLogman().info("Enabling "+getName() + " Version " + getVersion()); 
			getLogman().info("Authored by "+getAuthor());
			Canary.commands().registerCommands(this, this, false);
			return true;
		} catch (CommandDependencyException e){
			throw new RuntimeException(e);
		}
	}

	//API TIME


	//Activate commands-> Set up relAative position
	// 0,0,0 is player location -> forward is player direction


	//Turn On
	@Command(
			aliases = { "ton, TurtleOn" },
			description = "Turtle On",
			permissions = { "" },
			toolTip = "/ton")
	public void TurtleOn(MessageReceiver sender, String[] args)
	{
		//Relative pos stuff
		
		//Get True Position and Direction
		truePos = sender.asPlayer().getPosition();
		trueDir = sender.asPlayer().getCardinalDirection();
		
		//Make the Relative Position
		relPos = new Position(0,0,0);
		relDir = Direction.getFromIntValue(0);
		updateCurPos();
		updateCurDir();
		//Faces player direction
		//Need to build in safety checks
		//Also, better way?
		//If doesn't work-> set to North ONLY, as way to start debugging
		relDir = trueDir; //??
		
		
		


		//Turning on Turtle
		tt = true;
		getString(sender, truePos);
		getString(sender, trueDir);
		getString(sender, tt);
	}
	//Turn off
	@Command(
			aliases = { "toff, TurtleOff" },
			description = "Turtle Off",
			permissions = { "" },
			toolTip = "/toff")
	public void TurtleOff(MessageReceiver sender, String[] args)
	{
		//Turning off Turtle
		tt = false;
		turtle = null;
		getString(sender, tt);
	}

	//Turn on/off (toggle)
	@Command(
			aliases = { "tt" },
			description = "Turtle Toggle",
			permissions = { "" },
			toolTip = "/tt")
	public void TurtleToggle(MessageReceiver sender, String[] args)
	{
		if (tt)
		{ //if On, Turning off Turtle
			TurtleOff(sender, args);
		}
		else
		{ //if Off, Turning on Turtle
			TurtleOn(sender, args);
		}
	}


	//Other Commands*******************************************************************************

	//Console output
	@Command(
			aliases = { "c" },
			description = "Turtle Console",
			permissions = { "" },
			toolTip = "/c + 'String'")
	public void TurtleConsole(MessageReceiver sender, String[] args)
	{
		//Display string in console
		String message = "";
	    for (int i=1; i<args.length; i++) {  //skip the command, just send the message
	    	message = message + args[i]+ " ";
	    }
	    
	    sender.message(message); 

	}

	//Place Block on/off -> IF placement off -> dont change vs AIr placement)
	@Command(
			aliases = { "bp" },
			description = "Turtle Block Placement",
			permissions = { "" },
			toolTip = "/bp")
	public void TurtleBlockPlace(MessageReceiver sender, String[] args)
	{
		bp = !bp;
		TurtleBlockPlaceStatus(sender, args);
	}
	
	
	//Placement On? (status)
	@Command(
			aliases = { "bp?" },
			description = "Turtle Block Placement",
			permissions = { "" },
			toolTip = "/bp?")
	public void TurtleBlockPlaceStatus(MessageReceiver sender, String[] args)
	{
		getString(sender, bp);
	}
	
	
	//Set position (based on relative grid)
	@Command(
			aliases = { "sp" },
			description = "Turtle Setpos",
			permissions = { "" },
			toolTip = "/sp'")
	public void TurtleSetPosition(MessageReceiver sender, String[] args)
	{
		//Chnage location to new location based on relative coordinates
		relPos = new Position (Integer.parseInt(args[1]), Integer.parseInt(args[2]),Integer.parseInt(args[3]));
	}
	//Set direction (textbased)(North, South, East, West)
	//Number based for simplicity in early tests?

	@Command(
			aliases = { "sd" },
			description = "Turtle Setdir",
			permissions = { "" },
			toolTip = "/sd'")
	public void TurtleSetDirection(MessageReceiver sender, String[] args)
	{
		relDir = Direction.getFromIntValue(Integer.parseInt(args[1]));
		// 0 = NORTH
		// 2 = EAST
		// 4 = SOUTH
		// 6 = WEST
		// Else = ERROR

	}

	//Return current pos
	@Command(
			aliases = { "rp" },
			description = "Turtle retpos",
			permissions = { "" },
			toolTip = "/rp'")
	public void TurtleReturnPosition(MessageReceiver sender, String[] args)
	{
		//return position of turtle (relative position)
		
		//   getRelPos();
		getString(sender, relPos);

	}
	
	//return Direction (status)
	@Command(
			aliases = { "rd" },
			description = "Turtle retdir",
			permissions = { "" },
			toolTip = "/rd'")
	public void TurtleReturnDirection(MessageReceiver sender, String[] args)
	{
		//return position of turtle	
		getString(sender, relDir);
	}
	
	//set Block Type (int based)
	@Command(
			aliases = { "sb" },
			description = "Turtle set bt",
			permissions = { "" },
			toolTip = "/sb'")
	public void TurtleSetBlockType(MessageReceiver sender, String[] args)
	{
		//set current BT of turtle	
		BlockType temp = BlockType.fromIdAndData(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		bt = temp;

	}

	//set Block type (string/BlockType based)
	//TODO implementation

	//return block type
	@Command(
			aliases = { "rb" },
			description = "Turtle ret bt",
			permissions = { "" },
			toolTip = "/rb'")
	public void TurtleReturnBlockType(MessageReceiver sender, String[] args)
	{
		//return current BT of turtle	
		getString(sender, bt);
	}


	//move (forward/back)
	@Command(
			aliases = { "m" },
			description = "Turtle move",
			permissions = { "" },
			toolTip = "/m'")
	public void TurtleMove(MessageReceiver sender, String[] args)
	{
		//move turtle	
		// Move should act in a loop to go 1 -> just happens. To go 10, loop 10 times
		//Allows easier pos/ bp coding
		
		for (int i = Integer.parseInt(args[1]); i > 0; i--){
			//If block place True
			if (bp) {
				
		    	world.setBlockAt(relPos, bt);
			}else {
				//Place nothing
			}
			
			//relPos = turtle.move(relPos, relDir, false);
		}
		
	}

	//turn (number based) (degrees)
	//TODO implementation -> will allow diagonals

	//turn (Right/Left) (text based)
	@Command(
			aliases = { "t" },
			description = "Turtle turn",
			permissions = { "" },
			toolTip = "/t'")
	public void TurtleTurn(MessageReceiver sender, String[] args)
	{
		//turn turtle (left or right)

	}

	@HookHandler
	public void uploadJSON(UploadJSONHook hook) { 
	    logger.info("Hook called");
	    JSONParser parser=new JSONParser();
	    try {
	        logger.info(hook.getJSON());
	        JSONObject json=(JSONObject)parser.parse(hook.getJSON());
	        
	        String scriptname=(String)json.get("scriptname");
	        logger.info(String.format("%s\n", scriptname));
	        
	        JSONArray lang= (JSONArray) json.get("commands");
	        for (int i=0; i<lang.size(); i++) {
	            JSONObject cmd=(JSONObject)lang.get(i);
	            String commandName=(String)cmd.get("cmd");
	            JSONObject args=(JSONObject)cmd.get("args");
	            if (commandName.equals("forward")) {
	                //int distance=getInt(args, "dist");
	                long distance=(long)args.get("dist");
	                // Move forward by the appropriate distance
	                logger.info(String.format("Move forward by %d\n", distance));
	            } else if (commandName.equals("turn")) {
	                String dir=(String)args.get("dir");
	                //int degrees=getInt(args, "degrees");
	                long degrees=(long)args.get("degrees");
	                logger.info(String.format("turn %s %d degrees\n", dir, degrees));
	            }
	        }
	    } catch (ParseException e) {
	        throw new RuntimeException(e);
	    }
	}
}
