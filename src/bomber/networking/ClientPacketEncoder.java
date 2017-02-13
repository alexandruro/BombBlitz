package bomber.networking;

import java.awt.Point;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import bomber.game.AudioEvent;
import bomber.game.Block;
import bomber.game.Bomb;
import bomber.game.GameState;
import bomber.game.KeyboardState;
import bomber.game.Map;
import bomber.game.Movement;
import bomber.game.Player;

/**
 * 
 * Methods for encoding and decoding more complex packets for the client
 *
 */
public class ClientPacketEncoder {

	/**
	 * Decode List of ClientServerPlayer from bytes in MSG_S_LOBBY_PLAYERLIST
	 * format. The first three bytes in the destination byte array are reserved
	 * for message type and sequence number and will be ignored. The caller
	 * should ensure the first three bytes are correct before calling this
	 * method.
	 * 
	 * @param src
	 *            the source byte array
	 * @param length
	 *            the length of the data in the byte array
	 * @return a list of ClientServerPlayer
	 * @throws IOException
	 */
	public static List<ClientServerPlayer> decodePlayerList(byte[] src, int length) throws IOException {
		if (src == null) {
			throw new IOException("src is null");
		}

		if (length > src.length) {
			throw new IOException("length is invalid");
		}

		if (length < 1 + 2 + 4 + 4 + 4 + 4) {
			throw new IOException("packet format is invalid");
		}

		ByteBuffer buffer = ByteBuffer.wrap(src, 0, length);
		buffer.position(3);

		// int totalPlayers = buffer.getInt();
		// int packetIndex = buffer.getInt();
		// int maxIndex = buffer.getInt();
		buffer.position(15);
		int numPlayers = buffer.getInt();

		// System.out.printf("%d %d %d %d\n", totalPlayers, packetIndex,
		// maxIndex, numPlayers);

		buffer.position(19);
		List<ClientServerPlayer> playerList = new ArrayList<ClientServerPlayer>();
		for (int i = 0; i < numPlayers; i++) {
			if (length < buffer.position() + 4) {
				throw new IOException("packet format is invalid");
			}
			int id = buffer.getInt();

			if (length < buffer.position() + 1) {
				throw new IOException("packet format is invalid");
			}
			byte nameLength = buffer.get();

			if (nameLength < 1 || length < buffer.position() + nameLength) {
				throw new IOException("packet format is invalid");
			}
			byte[] nameData = new byte[nameLength];
			buffer.get(nameData);
			String name = new String(nameData, 0, nameLength, "UTF-8");

			playerList.add(new ClientServerPlayer(id, name));
		}

		if (length != buffer.position()) {
			throw new IOException("packet format is invalid");
		}

		return playerList;
	}

	/**
	 * Decode List of ClientServerRoom from bytes in MSG_S_LOBBY_ROOMLIST
	 * format. The first three bytes in the destination byte array are reserved
	 * for message type and sequence number and will be ignored. The caller
	 * should ensure the first three bytes are correct before calling this
	 * method.
	 * 
	 * @param src
	 *            the source byte array
	 * @param length
	 *            the length of the data in the byte array
	 * @return a list of ClientServerRoom
	 * @throws IOException
	 */
	public static List<ClientServerLobbyRoom> decodeRoomList(byte[] src, int length) throws IOException {
		if (src == null) {
			throw new IOException("src is null");
		}

		if (length > src.length) {
			throw new IOException("length is invalid");
		}

		if (length < 1 + 2 + 4 + 4 + 4 + 4) {
			throw new IOException("packet format is invalid");
		}

		ByteBuffer buffer = ByteBuffer.wrap(src, 0, length);
		buffer.position(3);

		// int totalPlayers = buffer.getInt();
		// int packetIndex = buffer.getInt();
		// int maxIndex = buffer.getInt();
		buffer.position(15);
		int numRooms = buffer.getInt();

		// System.out.printf("%d %d %d %d\n", totalPlayers, packetIndex,
		// maxIndex, numPlayers);

		buffer.position(19);
		List<ClientServerLobbyRoom> roomList = new ArrayList<ClientServerLobbyRoom>();
		for (int i = 0; i < numRooms; i++) {
			// get room id
			if (length < buffer.position() + 4) {
				throw new IOException("packet format is invalid");
			}
			int id = buffer.getInt();

			// get room name length
			if (length < buffer.position() + 1) {
				throw new IOException("packet format is invalid");
			}
			byte nameLength = buffer.get();

			// get room name
			if (nameLength < 1 || length < buffer.position() + nameLength) {
				throw new IOException("packet format is invalid");
			}
			byte[] nameData = new byte[nameLength];
			buffer.get(nameData);
			String name = new String(nameData, 0, nameLength, "UTF-8");

			// get player number
			if (length < buffer.position() + 1) {
				throw new IOException("packet format is invalid");
			}
			byte playerNumber = buffer.get();

			// get max player limit
			if (length < buffer.position() + 1) {
				throw new IOException("packet format is invalid");
			}
			byte maxPlayer = buffer.get();

			// get inGame boolean flag
			if (length < buffer.position() + 1) {
				throw new IOException("packet format is invalid");
			}
			boolean inGame = false;
			if (buffer.get() == 1) {
				inGame = true;
			}

			// get game map ID
			if (length < buffer.position() + 4) {
				throw new IOException("packet format is invalid");
			}
			int mapID = buffer.getInt();

			roomList.add(new ClientServerLobbyRoom(id, name, playerNumber, maxPlayer, inGame, mapID));
		}

		if (length != buffer.position()) {
			throw new IOException("packet format is invalid");
		}

		return roomList;
	}

	// The Serious Part II
	/**
	 * Decode GameState from bytes in MSG_S_ROOM_GAMESTATE format. The first
	 * three bytes in the destination byte array are reserved for message type
	 * and sequence number and will be ignored. The caller should ensure the
	 * first three bytes are correct before calling this method.
	 * 
	 * @param src
	 *            the source byte array
	 * @param length
	 *            the length of the data in the byte array
	 * @return a list of ClientServerRoom
	 * @throws IOException
	 */
	public static GameState decodeGameState(byte[] src, int length) throws IOException {
		if (src == null) {
			throw new IOException("src is null");
		}

		if (length > src.length) {
			throw new IOException("length is invalid");
		}

		if (length < 7 + 130 + 1) {
			throw new IOException("packet format is invalid");
		}

		ByteBuffer buffer = ByteBuffer.wrap(src, 0, length);

		buffer.position(7);
		byte gridMapWidth = buffer.get();
		if (gridMapWidth < 1 || gridMapWidth > 16) {
			throw new IOException("gridMap width is not in the range [1,16]");
		}

		byte gridMapHeight = buffer.get();
		if (gridMapHeight < 1 || gridMapHeight > 16) {
			throw new IOException("gridMap height is not in the range [1,16]");
		}

		Block[][] gridMap = new Block[gridMapWidth][gridMapHeight];

		buffer.position(7 + 130);
		byte numPlayer = buffer.get();
		if (numPlayer < 1) {
			throw new IOException("playerList size is not in the range [1,127]");
		}

		if (length < 7 + 130 + 1 + 35 * numPlayer + 1) {
			throw new IOException("packet format is invalid");
		}

		buffer.position(7 + 130 + 1 + 35 * numPlayer);
		byte numBomb = buffer.get();
		if (numBomb < 0) {
			throw new IOException("bombList size is not in the range [0,127]");
		}

		if (length != 7 + 130 + 1 + 35 * numPlayer + 1 + 20 * numBomb + 2) {
			throw new IOException("packet format is invalid");
		}

		// Map
		buffer.position(7 + 1 + 1);
		long bitArr[][] = new long[4][4];
		for (int b = 3; b >= 0; b--) {
			for (int i = 3; i >= 0; i--) {
				bitArr[b][i] = buffer.getLong();
			}
		}

		for (int x = 0; x < gridMapWidth; x++) {
			for (int y = 0; y < gridMapHeight; y++) {
				Block b = Block.BLANK;

				boolean bits[] = new boolean[4];
				int bitIndex = x + y * gridMapWidth;

				for (int i = 3; i >= 0; i--) {
					bits[i] = BitArray.getBit(bitArr[i][bitIndex / 64], bitIndex % 64);
				}

				if (!bits[3] && !bits[2] && !bits[1] && !bits[0]) {
					b = Block.BLANK;
				} else if (!bits[3] && !bits[2] && !bits[1] && bits[0]) {
					b = Block.BLAST;
				} else if (!bits[3] && !bits[2] && bits[1] && !bits[0]) {
					b = Block.SOFT;
				} else if (!bits[3] && !bits[2] && bits[1] && bits[0]) {
					b = Block.SOLID;
				}

				gridMap[x][y] = b;
			}
		}

		// Players
		buffer.position(7 + 130 + 1);
		List<Player> playerList = new ArrayList<>(numPlayer);

		for (int i = 0; i < numPlayer; i++) {
			int id = buffer.getInt();
			int posX = buffer.getInt();
			int posY = buffer.getInt();
			int lives = buffer.getInt();
			double speed = buffer.getDouble();
			int bombRange = buffer.getInt();
			int maxBomb = buffer.getInt();
			short keyState = buffer.getShort();
			byte alive = buffer.get();

			boolean isAlive = alive == 1;
			KeyboardState k = new KeyboardState();
			k.setMovement(Movement.NONE);
			k.setBomb(false);

			if (BitArray.getBit(keyState, 0)) {
				k.setMovement(Movement.NONE);
			} else if (BitArray.getBit(keyState, 1)) {
				k.setMovement(Movement.UP);
			} else if (BitArray.getBit(keyState, 2)) {
				k.setMovement(Movement.DOWN);
			} else if (BitArray.getBit(keyState, 3)) {
				k.setMovement(Movement.LEFT);
			} else if (BitArray.getBit(keyState, 4)) {
				k.setMovement(Movement.RIGHT);
			}
			k.setBomb(BitArray.getBit(keyState, 5));

			Player p = new Player("player " + id, new Point(posX, posY), lives, speed, null);
			p.setPlayerID(id);
			p.setBombRange(bombRange);
			p.setMaxNrOfBombs(maxBomb);
			p.setKeyState(k);
			p.setAlive(isAlive);

			playerList.add(p);
		}

		// Bombs
		buffer.position(7 + 130 + 1 + 35 * numPlayer + 1);
		List<Bomb> bombList = new ArrayList<>(numBomb);

		for (int i = 0; i < numBomb; i++) {
			int playerID = buffer.getInt();
			int posX = buffer.getInt();
			int posY = buffer.getInt();
			int time = buffer.getInt();
			int radius = buffer.getInt();

			Bomb b = new Bomb("player " + playerID, new Point(posX, posY), time, radius);
			b.setPlayerID(playerID);

			bombList.add(b);
		}

		// Audio Events
		buffer.position(7 + 130 + 1 + 35 * numPlayer + 1 + 20 * numBomb);
		short audioState = buffer.getShort();

		List<AudioEvent> audioEventList = new ArrayList<>(16);
		if (BitArray.getBit(audioState, 0)) {
			audioEventList.add(AudioEvent.PLACE_BOMB);
		}
		if (BitArray.getBit(audioState, 1)) {
			audioEventList.add(AudioEvent.EXPLOSION);
		}
		if (BitArray.getBit(audioState, 2)) {
			audioEventList.add(AudioEvent.PLAYER_DEATH);
		}

		GameState gameState = new GameState(new Map(gridMap), playerList);
		gameState.setBombs(bombList);
		gameState.setAudioEvents(audioEventList);

		return gameState;
	}

}
