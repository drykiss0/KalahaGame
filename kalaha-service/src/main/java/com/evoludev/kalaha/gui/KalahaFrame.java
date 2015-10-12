package com.evoludev.kalaha.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.evoludev.kalaha.game.KalahaGame;
import com.evoludev.kalaha.game.Player;
import com.google.common.base.Splitter;

public class KalahaFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private interface PitComponent {
		void updateFromSeedCount(int seeds);
	}
	
	private class HouseButton extends JButton implements PitComponent {
		
		private static final long serialVersionUID = 1L;
		private int ordinal;
		
		public HouseButton(String text) {
			super(text);
		}

		@Override
		public void updateFromSeedCount(int seeds) {
			setText(String.valueOf(seeds));
		}

		public int getOrdinal() {
			return ordinal;
		}

		public void setOrdinal(int ordinal) {
			this.ordinal = ordinal;
		}
	};
	
	private class StoreLabel extends JLabel implements PitComponent {
		
		private static final long serialVersionUID = 1L;
		public StoreLabel(String text) {
			super(text);
		}
		@Override
		public void updateFromSeedCount(int seeds) {
			setText(String.valueOf(seeds));
		}
	}
	
	private class KalahaStateHelper {
		private String currentPlayerName;
		private List<Integer> pitSeedCounts = new ArrayList<>();
		
		public KalahaStateHelper parseKalahaState() {
			List<Integer> boardState = Splitter.onPattern("\\|").splitToList(kalaha.getBoardState()).stream()
					.map(s -> Integer.valueOf(s)).collect(Collectors.toList());
			pitSeedCounts.addAll(boardState.subList(1, boardState.size()));
			currentPlayerName = kalaha.getPlayerToMove().getName();
			return this;
		}
	
		public String getCurrentPlayerName() {
			return currentPlayerName;
		}
	
		public List<Integer> getPitSeedCounts() {
			return pitSeedCounts;
		}
	}

	private List<PitComponent> pitComponents = new ArrayList<>();
	private Map<Integer, List<HouseButton>> playersHouseButtons = new HashMap<>();
	private Map<Integer, StoreLabel> playersStoreLabels = new HashMap<>();
	private KalahaGame kalaha;
	private JLabel status;
	
	public KalahaFrame(String name) {
		super(name);
		setResizable(false);
		kalaha = KalahaGame.newGame("Player 1", "Player 2");
	}

	public void init() {

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
		
		initKalahaComponents();

		BorderLayout mainLayout = new BorderLayout(10,5);
		mainPanel.setLayout(mainLayout);
		mainPanel.add(new JPanel(), BorderLayout.PAGE_START);

		GridLayout centerLayout = new GridLayout(2, 6);
		centerLayout.setHgap(10);
		centerLayout.setVgap(60);
		JPanel centerPanel = new JPanel(centerLayout);
		centerPanel.setPreferredSize(new Dimension(700, 200));

		// Not possible? in GridLayout to assign position to each component
		for (int i = playersHouseButtons.get(1).size() - 1; i >= 0; i--) {
			centerPanel.add(playersHouseButtons.get(1).get(i));
		}
		for (JButton b: playersHouseButtons.get(0)) {
			centerPanel.add(b);			
		}
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		mainPanel.add(playersStoreLabels.get(0), BorderLayout.LINE_END);
		mainPanel.add(playersStoreLabels.get(1), BorderLayout.LINE_START);
		
		status = new JLabel(kalaha.getPlayerToMove().getName() + " to go");
		status.setFont(new Font("Verdana", Font.PLAIN, 20));
		status.setForeground(kalaha.getPlayerToMove().getIndex() == 0 ? Color.GREEN : Color.RED);		
		JPanel statusPanel = new JPanel();
		statusPanel.add(status);
		mainPanel.add(statusPanel, BorderLayout.PAGE_END);

		getContentPane().add(mainPanel);
	}
	
	private void updateBoard() {
		
		KalahaStateHelper stateHelper = new KalahaStateHelper().parseKalahaState();
		for (int c = 0; c < pitComponents.size(); c++) {
			pitComponents.get(c).updateFromSeedCount(stateHelper.getPitSeedCounts().get(c));
		}
		for (Player player: kalaha.getPlayers()) {
			playersStoreLabels.get(player.getIndex()).setText(String.valueOf(player.getStore().getSeeds()));			
		}
		
		if (!kalaha.isGameFinished()) {
			status.setText(stateHelper.getCurrentPlayerName() + " to go");
			status.setForeground(kalaha.getPlayerToMove().getIndex() == 0 ? Color.GREEN : Color.RED);
			
			playersHouseButtons.get(kalaha.getPlayerToMove().getIndex()).forEach(h -> h.setEnabled(true));
			playersHouseButtons.get(kalaha.getPlayerToMove().getNextPlayer().getIndex()).forEach(h -> h.setEnabled(false));
		} else {
			
			playersHouseButtons.values().forEach(c->c.stream().forEach(h -> h.setEnabled(false)));
			if (kalaha.getWinningPlayer().isPresent()){
				status.setText("Winner is " + kalaha.getWinningPlayer().get().getName());
				status.setForeground(kalaha.getWinningPlayer().get().getIndex() == 0 ? Color.GREEN : Color.RED);				
			} else {
				status.setText("It's a draw!");
				status.setForeground(Color.BLACK);
			}
		}
	}

	private void initKalahaComponents() {
		
		// TODO: refactor player attributes such as color to separate class
		playersHouseButtons.put(0, createHouseButtons(KalahaGame.HOUSES_PER_PLAYER, String.valueOf(KalahaGame.INITIAL_SEEDS_COUNT), Color.GREEN));
		playersStoreLabels.put(0,  createStoreLabel("0", Color.GREEN));
		playersHouseButtons.put(1, createHouseButtons(KalahaGame.HOUSES_PER_PLAYER, String.valueOf(KalahaGame.INITIAL_SEEDS_COUNT), Color.RED));
		playersStoreLabels.put(1,  createStoreLabel("0", Color.RED));		
		
		pitComponents.addAll(playersHouseButtons.get(0));
		pitComponents.add(playersStoreLabels.get(0));
		pitComponents.addAll(playersHouseButtons.get(1));
		pitComponents.add(playersStoreLabels.get(1));	
		
		playersHouseButtons.get(1).forEach(b -> b.setEnabled(false));
	}

	private StoreLabel createStoreLabel(String text, Color color) {
		StoreLabel storeLabel = new StoreLabel(text);
		storeLabel.setFont(new Font("Verdana", Font.BOLD, 34));
		storeLabel.setForeground(color);
		return storeLabel;
	}
	
	private List<HouseButton> createHouseButtons(int howMany, String text, Color color) {
		
		List<HouseButton> houseButtons = new ArrayList<>();
		Font font = new Font("Verdana", Font.BOLD, 26);		
		for (int i = 0; i < howMany; i++) {
			HouseButton houseButton = new HouseButton(text);
			houseButton.setHorizontalAlignment(SwingConstants.CENTER);
			houseButton.setFont(font);
			houseButton.setForeground(color);
			houseButton.setOrdinal(i);
			houseButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!"0".equals(houseButton.getText().trim())) {
						kalaha.makeMove(houseButton.getOrdinal());
						updateBoard();
					}
				}
			});
			houseButtons.add(houseButton);
		}		
		return houseButtons;
	}
}
