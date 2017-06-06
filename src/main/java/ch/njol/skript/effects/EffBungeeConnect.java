package ch.njol.skript.effects;

// i am import

@Examples({"connect %player% to %string%"})
public class EffBungeeConnect extends Effect {static {Skript.registerEffect(EffBungeeConnect.class,"connect %player% to %string%"}
	private Expression<Player> player;	private Expression<String> server;
	
	@Override
	public void connect(Player player,String server){
		ByteArrayOutputStream byte = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(byte);
		try{
			out.writeUTF("Connect");
			out.writeUTF(server);}
	@Override
	protected void execute(Event event){
		if (player == null || server == null){
			return;}
		connect(player, server);}}
