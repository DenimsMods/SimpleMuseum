package denimred.simplemuseum.client.resources.data;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;
import java.util.List;

public class ExpressionDataSerializer implements MetadataSectionSerializer<ExpressionDataSection> {

    @Override
    public ExpressionDataSection fromJson(JsonObject json) {
        List<ExpressionData> expressionList = Lists.newArrayList();

        // Expressions
        if (json.has("expressions")) {
            try {
                JsonArray expressionArray = GsonHelper.getAsJsonArray(json, "expressions");
                for(int i = 0; i < expressionArray.size(); ++i) {
                    JsonElement expressionElement = expressionArray.get(i);
                    ExpressionData expression = this.getExpression(i, expressionElement);
                    if (expression != null) {
                        expressionList.add(expression);
                    }

                }
            } catch (ClassCastException classcastexception) {
                throw new JsonParseException("Invalid model data at 'expressions': expected array, was " + json.get("expressions"), classcastexception);
            }
        }

        return new ExpressionDataSection(expressionList);
    }

    private ExpressionData getExpression(int id, JsonElement expressionElement) {
        if (expressionElement.isJsonObject()) {

            JsonObject jsonobject = GsonHelper.convertToJsonObject(expressionElement, "expressions[" + id + "]");
            String name = GsonHelper.getAsString(jsonobject, "name", "");
            boolean interpolated = GsonHelper.getAsBoolean(jsonobject, "interpolated", false);

            return new ExpressionData(id, name, interpolated, getAreas(jsonobject));
        } else {
            return null;
        }
    }

    private List<ExpressionFaceData> getAreas(JsonObject jsonobject) {
        List<ExpressionFaceData> areaList = Lists.newArrayList();
        JsonArray areaArray = jsonobject.has("areas") ? GsonHelper.getAsJsonArray(jsonobject, "areas") : null;
        // Areas
        if (areaArray != null) {
            try {
                for(int j = 0; j < areaArray.size(); ++j) {
                    JsonElement areaElement = areaArray.get(j);
                    ExpressionFaceData area = this.getArea(j, areaElement);
                    if (area != null) {
                        areaList.add(area);
                        System.out.println("added area");
                    } else
                        System.out.println("didnt add area");

                }
            } catch (ClassCastException classcastexception) {
                throw new JsonParseException("Invalid model data at 'areas': expected array, was " + jsonobject.get("areas"), classcastexception);
            }
        } else{
            System.out.println("Found no areas");
        }
        return areaList;
    }
    private ExpressionFaceData getArea(int id, JsonElement areaElement) {
        if (areaElement.isJsonObject()) {

            JsonObject jsonobject = GsonHelper.convertToJsonObject(areaElement, "areas[" + areaElement + "]");
            String bone = GsonHelper.getAsString(jsonobject, "bone", "");
            int cube = GsonHelper.getAsInt(jsonobject, "cube", 0);
            String faceStr = GsonHelper.getAsString(jsonobject, "face", "NORTH");
            Direction face = Direction.valueOf(faceStr);
            float[] newPos = new float[]{ GsonHelper.getAsJsonArray(jsonobject, "newPos").get(0).getAsFloat(), GsonHelper.getAsJsonArray(jsonobject, "newPos").get(1).getAsFloat() };

            System.out.println("New Area: " + id + ", " + bone + ", " + cube + ", " + face + ", " + Arrays.toString(newPos));
            return new ExpressionFaceData(id, bone, cube, face, newPos);
        } else {
            return null;
        }
    }

    @Override
    public String getMetadataSectionName() {
        return "model";
    }
}
