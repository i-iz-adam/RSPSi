package com.rspsi.plugin.loader;

import com.displee.cache.index.archive.Archive;
import com.displee.cache.index.archive.file.File;

import java.util.Arrays;
import java.util.List;

import lombok.val;
import org.apache.commons.compress.utils.Lists;

import com.jagex.cache.anim.Animation;
import com.jagex.cache.loader.anim.AnimationDefinitionLoader;
import com.jagex.io.Buffer;

public class AnimationDefinitionLoaderOSRS extends AnimationDefinitionLoader {


	private int count;
	private Animation[] animations;
	
	@Override
	public void init(Archive archive) {
		val highestId = Arrays.stream(archive.fileIds()).max().getAsInt();
		animations = new Animation[highestId + 1];
		for(File file : archive.files()) {
			if(file != null && file.getData() != null) {
				animations[file.getId()] = decode(new Buffer(file.getData()));
			}
		}
		
	}

	@Override
	public void init(byte[] data) {
		Buffer buffer = new Buffer(data);
		count = buffer.readUShort();

		if (animations == null) {
			animations = new Animation[count];
		}

		for (int id = 0; id < count; id++) {

			animations[id] = decode(buffer);
		}
	}

	protected Animation decode(Buffer buffer) {
		Animation animation = new Animation();
		while (true) {
			int opcode = buffer.readUByte();
			if (opcode == 0)
				break;
			if (opcode == 1) {
				int frameCount = buffer.readUShort();
				int[] primaryFrames = new int[frameCount];
				int[] secondaryFrames = new int[frameCount];
				int[] durations = new int[frameCount];
				int frame;
				for (frame = 0; frame < frameCount; frame++)
					durations[frame] = buffer.readUShort();
				for (frame = 0; frame < frameCount; frame++) {
					primaryFrames[frame] = buffer.readUShort();
					secondaryFrames[frame] = -1;
				}
				for (frame = 0; frame < frameCount; frame++)
					primaryFrames[frame] = primaryFrames[frame] + (buffer.readUShort() << 16);
				animation.setFrameCount(frameCount);
				animation.setPrimaryFrames(primaryFrames);
				animation.setSecondaryFrames(secondaryFrames);
				animation.setDurations(durations);
				continue;
			}
			if (opcode == 2) {
				animation.setLoopOffset(buffer.readUShort());
				continue;
			}
			if (opcode == 3) {
				int count = buffer.readUByte();
				int[] interleaveOrder = new int[count + 1];
				for (int index = 0; index < count; index++)
					interleaveOrder[index] = buffer.readUByte();
				interleaveOrder[count] = 9999999;
				animation.setInterleaveOrder(interleaveOrder);
				continue;
			}
			if (opcode == 4) {
				animation.setStretches(true);
				continue;
			}
			if (opcode == 5) {
				animation.setPriority(buffer.readUByte());
				continue;
			}
			if (opcode == 6) {
				animation.setPlayerOffhand(buffer.readUShort());
				continue;
			}
			if (opcode == 7) {
				animation.setPlayerMainhand(buffer.readUShort());
				continue;
			}
			if (opcode == 8) {
				animation.setMaximumLoops(buffer.readUByte());
				continue;
			}
			if (opcode == 9) {
				animation.setAnimatingPrecedence(buffer.readUByte());
				continue;
			}
			if (opcode == 10) {
				animation.setWalkingPrecedence(buffer.readUByte());
				continue;
			}
			if (opcode == 11) {
				animation.setReplayMode(buffer.readUByte());
				continue;
			}
			if (opcode == 12) {
				int len = buffer.readUByte();
				int i;
				for (i = 0; i < len; i++)
					buffer.readUShort();
				for (i = 0; i < len; i++)
					buffer.readUShort();
				continue;
			}
			if (opcode == 13) {
				int len = buffer.readUByte();
				for (int i = 0; i < len; i++)
					buffer.skip(5);
				continue;
			}
			if (opcode == 14) {
				buffer.skip(4);
				continue;
			}
			if (opcode == 15) {
				int count = buffer.readUShort();
				buffer.skip(count * 7);
				continue;
			}
			if (opcode == 16) {
				buffer.skip(4);
				continue;
			}
			if (opcode == 17) {
				int count = buffer.readUByte();
				buffer.skip(count);
				continue;
			}
			System.err.println("Error unrecognised seq config code: " + opcode);
		}
		if (animation.getFrameCount() == 0) {
			animation.setFrameCount(1);
			int[] primaryFrames = new int[1];
			primaryFrames[0] = -1;
			int[] secondaryFrames = new int[1];
			secondaryFrames[0] = -1;
			int[] durations = new int[1];
			durations[0] = -1;
			animation.setPrimaryFrames(primaryFrames);
			animation.setSecondaryFrames(secondaryFrames);
			animation.setDurations(durations);
		}
		if (animation.getAnimatingPrecedence() == -1)
			animation.setAnimatingPrecedence((animation.getInterleaveOrder() == null) ? 0 : 2);
		if (animation.getWalkingPrecedence() == -1)
			animation.setWalkingPrecedence((animation.getInterleaveOrder() == null) ? 0 : 2);
		return animation;
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public Animation forId(int id) {
		if(id < 0 || id > animations.length)
			id = 0;
		return animations[id];
	}



}
