package keelfy.sapr.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import keelfy.sapr.dto.Bar;
import keelfy.sapr.dto.Force;
import keelfy.sapr.dto.Load;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author e.kuzmin
 */
@Getter(AccessLevel.PROTECTED)
public abstract class SaprController {

    private final Map<Integer, Bar> bars = new HashMap<>();

    private final Map<Integer, Force> forces = new HashMap<>();

    private final Map<Integer, Load> loads = new HashMap<>();

    @Setter(AccessLevel.PROTECTED)
    private boolean leftBorder;

    @Setter(AccessLevel.PROTECTED)
    private boolean rightBorder;

    private final List<Double> nxks = new ArrayList<>();

    private final List<Double> nxbs = new ArrayList<>();

    private final List<Double> uxas = new ArrayList<>();

    private final List<Double> uxbs = new ArrayList<>();

    private final List<Double> uxcs = new ArrayList<>();

    private final List<Double> sigmaks = new ArrayList<>();

    private final List<Double> sigmabs = new ArrayList<>();

    protected void addBar(@NonNull Bar bar) {
        this.bars.put(bar.getId(), bar);
    }

    protected Bar getBar(@NonNull Integer index) {
        return this.bars.get(index);
    }

    protected int countBars() {
        return this.bars.size();
    }

    protected void removeBar(@NonNull Integer index) {
        this.bars.remove(index);
    }

    protected void addForce(@NonNull Force force) {
        this.forces.put(force.getId(), force);
    }

    protected Force getForce(@NonNull Integer index) {
        return this.forces.get(index);
    }

    protected int countForces() {
        return this.forces.size();
    }

    protected void removeForce(@NonNull Integer index) {
        this.forces.remove(index);
    }

    protected void addLoad(@NonNull Load load) {
        this.loads.put(load.getId(), load);
    }

    protected Load getLoad(@NonNull Integer index) {
        return this.loads.get(index);
    }

    protected int countLoads() {
        return this.loads.size();
    }

    protected void removeLoad(@NonNull Integer index) {
        this.loads.remove(index);
    }

    protected double getNxks(int index) {
        return this.nxks.get(index);
    }

    protected double getNxbs(int index) {
        return this.nxbs.get(index);
    }

    protected double getUxas(int index) {
        return this.uxas.get(index);
    }

    protected double getUxbs(int index) {
        return this.uxbs.get(index);
    }

    protected double getUxcs(int index) {
        return this.uxcs.get(index);
    }

    protected double getSigmaks(int index) {
        return this.sigmaks.get(index);
    }

    protected double getSigmabs(int index) {
        return this.sigmabs.get(index);
    }

    protected ObjectNode convertIntoJsonNode(ObjectMapper objectMapper) {
        final var root = objectMapper.createObjectNode();
        final var preProcessorNode = objectMapper.createObjectNode();

        final var barsNode = objectMapper.createArrayNode();
        for (final var bar : bars.values()) {
            final var barNode = objectMapper.createObjectNode();
            barNode.put("id", bar.getId());
            barNode.put("area", bar.getArea());
            barNode.put("elasticity", bar.getElasticity());
            barNode.put("length", bar.getLength());
            barNode.put("sigma", bar.getSigma());
            barsNode.add(barNode);
        }
        preProcessorNode.put("bars", barsNode);

        final var forcesNode = objectMapper.createArrayNode();
        for (final var force : forces.values()) {
            final var forceNode = objectMapper.createObjectNode();
            forceNode.put("id", force.getId());
            forceNode.put("value", force.getValue());
            forcesNode.add(forceNode);
        }
        preProcessorNode.put("forces", forcesNode);

        final var loadsNode = objectMapper.createArrayNode();
        for (final var load :loads.values()) {
            final var loadNode = objectMapper.createObjectNode();
            loadNode.put("id", load.getId());
            loadNode.put("value", load.getValue());
            loadsNode.add(loadNode);
        }
        preProcessorNode.put("loads", loadsNode);

        final var bordersNode = objectMapper.createObjectNode();
        bordersNode.put("left", leftBorder);
        bordersNode.put("right", rightBorder);
        preProcessorNode.put("borders", bordersNode);

        root.put("pre-processor", preProcessorNode);

        final var processorNode = objectMapper.createObjectNode();

        final var nxksNode = objectMapper.createArrayNode();
        this.nxks.forEach(nxksNode::add);
        processorNode.put("nxks", nxksNode);

        final var nxbsNode = objectMapper.createArrayNode();
        this.nxbs.forEach(nxbsNode::add);
        processorNode.put("nxbs", nxbsNode);

        final var uxasNode = objectMapper.createArrayNode();
        this.uxas.forEach(uxasNode::add);
        processorNode.put("uxas", uxasNode);

        final var uxbsNode = objectMapper.createArrayNode();
        this.uxbs.forEach(uxbsNode::add);
        processorNode.put("uxbs", uxbsNode);

        final var uxcsNode = objectMapper.createArrayNode();
        this.uxcs.forEach(uxcsNode::add);
        processorNode.put("uxcs", uxcsNode);

        final var sigmabsNode = objectMapper.createArrayNode();
        this.sigmabs.forEach(sigmabsNode::add);
        processorNode.put("sigmabs", sigmabsNode);

        final var sigmaksNode = objectMapper.createArrayNode();
        this.sigmaks.forEach(sigmaksNode::add);
        processorNode.put("sigmaks", sigmaksNode);

        root.put("processor", processorNode);

        return root;
    }

    protected void loadFromJson(JsonNode root) {
        final var preProcessor = root.path("pre-processor");

        this.bars.clear();
        for (final var node : preProcessor.path("bars")) {
            final var bar = new Bar(
                    node.path("id").asInt(),
                    node.path("area").asDouble(),
                    node.path("elasticity").asDouble(),
                    node.path("length").asDouble(),
                    node.path("sigma").asDouble()
            );
            this.bars.put(bar.getId(), bar);
        }

        this.forces.clear();
        for (final var node : preProcessor.path("forces")) {
            final var force = new Force(
                    node.path("id").asInt(),
                    node.path("value").asDouble()
            );
            this.forces.put(force.getId(), force);
        }

        this.loads.clear();
        for (final var node : preProcessor.path("loads")) {
            final var load = new Load(
                    node.path("id").asInt(),
                    node.path("value").asDouble()
            );
            this.loads.put(load.getId(), load);
        }

        leftBorder = preProcessor.path("borders").path("left").asBoolean();
        rightBorder = preProcessor.path("borders").path("right").asBoolean();

        final var processor = root.path("processor");

        this.nxks.clear();
        processor.path("nxks").forEach(node -> this.nxks.add(node.asDouble()));

        this.nxbs.clear();
        processor.path("nxbs").forEach(node -> this.nxbs.add(node.asDouble()));

        this.uxas.clear();
        processor.path("uxas").forEach(node -> this.uxas.add(node.asDouble()));

        this.uxbs.clear();
        processor.path("uxbs").forEach(node -> this.uxbs.add(node.asDouble()));

        this.uxcs.clear();
        processor.path("uxcs").forEach(node -> this.uxcs.add(node.asDouble()));

        this.sigmabs.clear();
        processor.path("sigmabs").forEach(node -> this.sigmabs.add(node.asDouble()));

        this.sigmaks.clear();
        processor.path("sigmaks").forEach(node -> this.sigmaks.add(node.asDouble()));

    }
}
