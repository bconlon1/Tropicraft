package net.tropicraft.core.common.dimension.feature.tree;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.tropicraft.core.common.block.CoconutBlock;
import net.tropicraft.core.common.block.TropicraftBlocks;
import net.tropicraft.core.common.block.TropicraftLeavesBlock;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

public abstract class PalmTreeFeature extends Feature<NoneFeatureConfiguration> {

    public PalmTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    protected SaplingBlock getSapling() {
        return TropicraftBlocks.PALM_SAPLING.get();
    }

    protected final BlockState getLeaf() {
        return TropicraftBlocks.PALM_LEAVES.get().defaultBlockState();
    }

    protected final BlockState getLog() {
        return TropicraftBlocks.PALM_LOG.get().defaultBlockState();
    }

    protected boolean isAir(LevelReader level, BlockPos pos) {
        return level.isEmptyBlock(pos);
    }

    protected void placeLeaf(Set<BlockPos> leaves, LevelSimulatedRW world, int x, int y, int z) {
        placeLeaf(leaves, world, new BlockPos(x, y, z));
    }

    protected void placeLeaf(Set<BlockPos> leaves, LevelSimulatedRW world, BlockPos pos) {
        // From FoliagePlacer
        if (TreeFeature.validTreePos(world, pos)) {
            leaves.add(pos.immutable());
            world.setBlock(pos, getLeaf(), 19);
        }
    }

    protected void placeLog(Set<BlockPos> logs, LevelSimulatedRW world, int x, int y, int z) {
        placeLog(logs, world, new BlockPos(x, y, z));
    }

    protected void placeLog(Set<BlockPos> logs, LevelSimulatedRW world, BlockPos pos) {
        if (TreeFeature.validTreePos(world, pos)) {
            logs.add(pos.immutable());
            world.setBlock(pos, getLog(), 19);
        }
    }

    private static final Direction[] DIRECTIONS = ArrayUtils.removeElement(Direction.values(), Direction.UP);

    public static void spawnCoconuts(LevelSimulatedRW world, BlockPos pos, RandomSource random, int chance, BlockState leaf) {
        BlockState coconut = TropicraftBlocks.COCONUT.get().defaultBlockState();
        for (Direction d : DIRECTIONS) {
            BlockPos pos2 = pos.relative(d);
            if (random.nextInt(chance) == 0 && TreeFeature.isAirOrLeaves(world, pos2)) {
                world.setBlock(pos2, coconut.setValue(CoconutBlock.FACING, d.getOpposite()), Block.UPDATE_ALL);
            }
        }
    }

    protected static DiscreteVoxelShape updateLeaves(LevelAccessor level, BoundingBox box, Set<BlockPos> logSet) {
        DiscreteVoxelShape voxelShape = new BitSetDiscreteVoxelShape(box.getXSpan(), box.getYSpan(), box.getZSpan());
        List<Set<BlockPos>> list = Lists.newArrayList();

        for (int j = 0; j < 7; ++j) {
            list.add(Sets.newHashSet());
        }

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int calculatedDistance = 0;
        list.getFirst().addAll(logSet);

        while (true) {
            while (calculatedDistance >= 7 || !list.get(calculatedDistance).isEmpty()) {
                if (calculatedDistance >= 7) {
                    return voxelShape;
                }

                Iterator<BlockPos> iterator = list.get(calculatedDistance).iterator();
                BlockPos blockPos = iterator.next();
                iterator.remove();
                if (box.isInside(blockPos)) {
                    if (calculatedDistance != 0) {
                        BlockState blockState = level.getBlockState(blockPos);
                        setBlockKnownShape(level, blockPos, blockState.setValue(BlockStateProperties.DISTANCE, calculatedDistance));
                    }
                    voxelShape.fill(blockPos.getX() - box.minX(), blockPos.getY() - box.minY(), blockPos.getZ() - box.minZ());

                    for (BlockPos offset : TropicraftLeavesBlock.AROUND_OFFSETS) {
                        mutablePos.setWithOffset(blockPos, offset);
                        if (box.isInside(mutablePos)) {
                            int x = mutablePos.getX() - box.minX();
                            int y = mutablePos.getY() - box.minY();
                            int z = mutablePos.getZ() - box.minZ();
                            if (!voxelShape.isFull(x, y, z)) {
                                BlockState blockState = level.getBlockState(mutablePos);
                                OptionalInt distance = TropicraftLeavesBlock.getOptionalDistanceAt(blockState);
                                if (distance.isPresent()) {
                                    int increasedDistance = Math.min(distance.getAsInt(), calculatedDistance + 1);
                                    if (increasedDistance < 7) {
                                        list.get(increasedDistance).add(mutablePos.immutable());
                                        calculatedDistance = Math.min(calculatedDistance, increasedDistance);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ++calculatedDistance;
        }
    }

    private static void setBlockKnownShape(LevelWriter level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 19);
    }
}
