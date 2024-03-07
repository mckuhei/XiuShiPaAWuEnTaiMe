package com.seosean.showspawntime.handler;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.seosean.showspawntime.utils.DebugUtils;
import com.seosean.showspawntime.utils.DelayedTask;
import com.seosean.showspawntime.utils.PlayerUtils;
import com.seosean.showspawntime.utils.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ScoreboardManager {
    private String title;
    private List<String> content;
    private boolean keepUpdate = false;
    private Minecraft minecraft;
    private DelayedTask delayedTask;
    public ScoreboardManager() {
        this.minecraft = Minecraft.getMinecraft();
        this.title = "";
        this.content = new ArrayList<>();
        this.delayedTask = new DelayedTask() {

            private int count = 0;
            @Override
            public void run() {
                if (keepUpdate || count == 20) {
                    this.cancel();
                }
                ScoreboardManager.this.updateScoreboardContent();
                ScoreboardManager.this.keepUpdate = PlayerUtils.isInZombiesTitle();
                count ++;
            }
        };
    }


    public void updateScoreboardContent() {
        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().theWorld == null) {
            return;
        }
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1);
        if (sidebarObjective == null) {
            return;
        }

        this.title = StringUtils.trim(sidebarObjective.getDisplayName());
        List<String> scoreboardLines = new ArrayList<>();
        Collection<Score> scores = scoreboard.getSortedScores(sidebarObjective);
        List<Score> filteredScores = scores.stream()
                .filter(p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#")).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        if (filteredScores.isEmpty()) {
            return;
        } else if (filteredScores.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(filteredScores, scores.size() - 15));
        } else {
            scores = filteredScores;
        }
        Collections.reverse(filteredScores);
        for (Score line : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(line.getPlayerName());
            String scoreboardLine = ScorePlayerTeam.formatPlayerName(team, line.getPlayerName()).trim();
            scoreboardLines.add(StringUtils.trim(scoreboardLine));
        }
        this.content = scoreboardLines;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getContents() {
        return content;
    }

    public String getContent(int row) {
        if (row > this.getSize()) {
            return "";
        }
        return content.get(row - 1);
    }

    public int getSize() {
        return content.size();
    }

    private int tick;
    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event){
        if (!this.keepUpdate) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.isSingleplayer()) {
            return;
        }

        if(event.phase != TickEvent.Phase.START) {
            return;
        }

        EntityPlayerSP p = mc.thePlayer;

        if(p == null) {
            return;
        }

        tick ++;

        if (tick != 0 && tick % 10 == 0) {
            this.updateScoreboardContent();
            tick = 0;
        }
    }

    @SubscribeEvent
    public void onPlayerChangeWorld(EntityJoinWorldEvent event) {
        if (!event.entity.worldObj.isRemote) {
            return;
        }
        if (minecraft == null || minecraft.theWorld == null || minecraft.isSingleplayer()) {
            return;
        }
        EntityPlayerSP p = minecraft.thePlayer;
        if (p == null) {
            return;
        }
        Entity entity = event.entity;
        if (!entity.equals(p)) {
            return;
        }

        this.startDetecting();
    }

    public void startDetecting() {
        this.delayedTask.runTaskTimer(0, 10);
    }
    public void setKeepUpdate(boolean flag) {
        this.keepUpdate = flag;
    }
}
