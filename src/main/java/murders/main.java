package murders;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class main extends PluginBase implements Listener {
	public static Set<Position> gameSpawns = new HashSet<>();
	public static Player murder;
	public static Player hero;
	public static List<Player> players = new ArrayList<>();

	@Override
	public void onEnable() {
		this.getLogger().info(TextFormat.colorize("&6Let's Play murder!"));
		this.getServer().getPluginManager().registerEvents(this, this);
		for (String pos : new Config(new File(this.getDataFolder(), "spawns.properties"), Config.PROPERTIES)
				.getStringList("spawns")) {
			main.gameSpawns.add(main.toStrFromPos(pos));
		}

	}

	@Override
	public void onDisable() {
		Config config = new Config(new File(this.getDataFolder(), "spawns.properties"), Config.PROPERTIES);
		HashSet<String> list = new HashSet<>();
		for (Position str : main.gameSpawns) {
			list.add(main.toPosFromStr(str));
		}
		config.set("spawns", list);
		config.save();
	}

	public void start() {
		main.murder = (Player) this.getServer().getOnlinePlayers().values().toArray()[rand(1,
				this.getServer().getOnlinePlayers().size() - 1)];
		main.hero = (Player) this.getServer().getOnlinePlayers().values().toArray()[rand(1,
				this.getServer().getOnlinePlayers().size() - 1)];
		if (murder.getName().equals(hero.getName())) {
			start();
		}
		Server.getInstance().getOnlinePlayers().values().forEach((Player player) -> {
			main.players.add(player);
			Position pos = (Position) main.gameSpawns.toArray()[main.rand(1, main.gameSpawns.size() - 1)];
			player.teleport(pos);
		});
	}

	public void stop(int type) {
		if (type == 0) {
			this.getServer().broadcastMessage(TextFormat.colorize("&b[생존팀 승리] &3생존팀이 승리하였습니다"));
			for (Player player : this.getServer().getOnlinePlayers().values()) {
				player.teleport(Server.getInstance().getDefaultLevel().getSpawnLocation());
			}
		} else {
			this.getServer().broadcastMessage(TextFormat.colorize("&c[생존팀 승리] &4살인자가 승리하였습니다"));
			for (Player player : this.getServer().getOnlinePlayers().values()) {
				player.teleport(Server.getInstance().getDefaultLevel().getSpawnLocation());
			}
		}

	}

	@EventHandler
	public void onSpawnAdd(PlayerInteractEvent e) {
		if (e.getPlayer().isOp()) {
			if (e.getItem().getId() == Item.CLAY) {
				main.gameSpawns.add(e.getPlayer());
			}
		}
	}

	@EventHandler
	public void onAtact(EntityDamageEvent event) {
		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event1 = (EntityDamageByEntityEvent) event;
			if (event1.getDamager() instanceof Player && event1.getEntity() instanceof Player) {

				if (event1.getDamager().equals(main.murder)) {
					event1.getEntity().attack(30);
					((Player) event1.getEntity()).sendMessage(TextFormat.colorize("&c[사망] &4당신은 머더러에게 사냥당하셨씁닌다"));
					return;
				} else if (event1.getDamager().equals(main.hero) && !event1.getEntity().equals(main.murder)) {
					event1.getEntity().attack(30);
					((Player) event1.getEntity()).sendMessage(TextFormat.colorize("&c[사망] &4당신은 히어로에게 사망당하셨습니다"));
					return;
				} else {
					event1.setCancelled();
					event.setCancelled();
				}
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		event.setDeathMessage("");
		if (event.getEntity().getName().equals(main.murder.getName())) {
			this.stop(0);
			return;
		}
		if (main.players.contains(event.getEntity())) {
			main.players.remove(event.getEntity());
			if (main.players.size() < 1) {
				this.stop(1);
			}
			if (event.getEntity().getName().equals(main.hero.getName())) {
				main.players.get(main.rand(1, main.players.size() - 1));
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if (event.getPlayer().getName().equals(murder.getName())) {
			this.stop(1);

			if (main.players.contains(event.getPlayer())) {
				main.players.remove(event.getPlayer());
				if (main.players.size() < 1) {
					this.stop(1);
				}
				if (event.getPlayer().getName().equals(main.hero.getName())) {
					main.players.get(main.rand(1, main.players.size() - 1));
				}
			}
		}
	}

	public static int rand(int min, int max) {
		return new Random().nextInt(max - min + 1) + min;
	}

	public static Position toStrFromPos(String str) {
		return new Position(Integer.parseInt(str.split(",")[0]), Integer.parseInt(str.split(",")[1]),
				Integer.parseInt(str.split(",")[2]), Server.getInstance().getLevelByName(str.split(",")[3]));
	}

	public static String toPosFromStr(Position pos) {
		return new StringBuilder().append(pos.getFloorX() + ",").append(pos.getFloorY() + ",")
				.append(pos.getFloorZ() + ",").append(pos.getLevel().getFolderName()).toString();
	}
}
