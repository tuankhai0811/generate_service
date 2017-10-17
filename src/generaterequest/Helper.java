/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package generaterequest;

import java.util.ArrayList;

/**
 *
 * @author Khai
 */
public class Helper {

    public static String output(String nameClass, String route, ArrayList<String> arrayInput) {
        String baseClass = "MainDTO";
        String baseRoute = "URL";

        StringBuilder output = new StringBuilder();
        int count = arrayInput.size();

        output.append("public class " + convertClassName(nameClass) + " extends " + baseClass + " {\n");
        output.append("    public final String " + baseRoute + " = \"" + route + "\";\n\n");
        output.append("    private String ");

        // Declare variable
        for (int i = 0; i < count; i++) {
            if (i == count - 1) {
                output.append(convertName(arrayInput.get(i)) + ";\n\n");
            } else {
                output.append(convertName(arrayInput.get(i)) + ", ");
            }
        }

        // Constructer
        output.append("    public " + convertClassName(nameClass) + "(");
        for (int i = 0; i < count; i++) {
            if (i == count - 1) {
                output.append("String " + convertName(arrayInput.get(i)) + ") {\n");
            } else {
                output.append("String " + convertName(arrayInput.get(i)) + ", ");
            }
        }
        for (int i = 0; i < count; i++) {
            String variable = convertName(arrayInput.get(i));
            output.append("        this." + variable + " = " + variable + ";\n");
        }
        output.append("    }\n\n");

        // Map
        output.append("    @Override\n");
        output.append("    public Map<String, Object> params() {\n");
        output.append("        Map<String, Object> params = new HashMap();\n");
        for (int i = 0; i < count; i++) {
            String variable = convertName(arrayInput.get(i));
            output.append("        params.put(\"" + arrayInput.get(i) + "\", " + variable + ");\n");
        }
        output.append("        return params;\n");
        output.append("    }\n\n");

        // path
        output.append("    @Override\n");
        output.append("    public String[] path() {\n");
        output.append("        return " + baseRoute + ".split(\"/\");\n");
        output.append("    }\n");

        // end
        output.append("}");

        return output.toString();
    }

    public static String convertName(String input) {
        StringBuilder result = new StringBuilder();
        result.append(input.substring(0, 1).toLowerCase());
        result.append(input.substring(1));
        return result.toString();
    }

    public static String convertClassName(String input) {
        StringBuilder result = new StringBuilder();
        result.append(input.substring(0, 1).toUpperCase());
        result.append(input.substring(1));
        return result.toString();
    }

    public static String getSpace(int tab) {
        String result = "";
        switch (tab) {
            case 0:
                result = "";
                break;
            case 1:
                result = "    ";
                break;
            case 2:
                result = "        ";
                break;
            case 3:
                result = "            ";
                break;
            case 4:
                result = "                ";
                break;
            case 5:
                result = "                    ";
                break;
            default:
                result = "";
        }
        return result;
    }

    public static String process(String source, String name, int tab) {
//		System.out.println("\n\n" + tab + "\n");
        ArrayList<Model> arrModel = new ArrayList<>();
        ArrayList<String> list = new ArrayList<>();
        String saveSource = source;
        if (source.charAt(0) == '[') {
            int numOpen = 0;
            int numClose = 0;
            for (int i = 0; i < source.length() - 1; i++) {
                if (source.charAt(i) == '{') {
                    numOpen += 1;
                }
                if (source.charAt(i) == '}') {
                    numClose += 1;
                }
                if (source.charAt(i) == '}' && numOpen == numClose && numOpen != 0) {
                    source = source.substring(3, i);
//					System.out.println("Yes" + source);
                    break;
                }
//				System.out.println(numOpen + "-" + numClose);
            }
        }

        source = source.trim();
        if (source.startsWith("{")) {
            source = source.substring(1, source.length() - 1);
        }
        if (tab == 0) {
            source = source.replace((char) 13, ' ');
            source = source.replace('\n', ' ');
            source = source.replaceAll("\n", "");
            source = source.replaceAll("    ", "");
            source = source.replaceAll(" ", "");
            source = source.replaceAll("\\\\\"", "");

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < source.length(); i++) {
                if (source.charAt(i) == '{' || source.charAt(i) == '[') {
                    builder.append('\"');
                }
                builder.append(source.charAt(i));
                if (source.charAt(i) == '}' || source.charAt(i) == ']') {
                    builder.append('\"');
                }
            }
            source = builder.toString();
        }
//		System.out.println(source);

        for (int index = 0; index < source.length(); index++) {
            int start = index, end;
            int num = 0;
            int numOpen = 0;
            int numEnd = 0;
            boolean isObject = true;
            for (int k = index; k < source.length(); k++) {
                if (source.charAt(k) == ':') {
                    if (source.charAt(k + 2) == '[') {
                        isObject = false;
                    }
                    // System.out.println(k);
                    break;
                }
            }
            // if (index<source.length()-1){
            // if (source.charAt(index+1) == '['){
            // isObject = false;
            // }
            // System.out.println(source.charAt(index+1));
            // }
            // System.out.println(isObject+"");
            for (int i = index; i < source.length(); i++) {
                if (isObject) {
                    if (source.charAt(i) == '\"') {
                        num += 1;
                    }

                    if (num % 2 == 0) {
                        if (source.charAt(i) == ',') {
                            end = i;
                            list.add(source.substring(start, end));
                            index = i;
                            break;
                        }
                    }
                    // model last
                    if (source.charAt(i) == '\"' && i == source.length() - 1) {
                        end = i;
                        list.add(source.substring(start, end));
                        index = i;
                        break;
                    }
                } else {
                    // Is Array
                    if (source.charAt(i) == '[') {
                        numOpen += 1;
                    }
                    if (source.charAt(i) == ']') {
                        numEnd += 1;
                    }
                    if (source.charAt(i) == '\"') {
                        if (numOpen == numEnd && numOpen != 0) {
                            end = i;
                            list.add(source.substring(start, end + 1));
                            index = i + 1;
                            break;
                        }
                    }
                    // model last
                    if (source.charAt(i) == '\"' && i == source.length() - 1) {
                        end = i;
                        list.add(source.substring(start, end));
                        index = i;
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < list.size(); i++) {
//			System.out.println(list.get(i));
        }

        for (int i = 0; i < list.size(); i++) {
            arrModel.add(createModel(list.get(i)));
        }

        for (int i = 0; i < arrModel.size(); i++) {
//			arrModel.get(i).toString();
        }

        // Create Class
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(getSpace(tab) + "public final class " + convertClassName(name) + " {\n");
        for (int i = 0; i < arrModel.size(); i++) {
            Model model = arrModel.get(i);
            String type;
            if (model.array) {
                type = model.getType() + "[]";
            } else {
                type = model.getType();
            }
            resultBuilder.append(getSpace(tab) + "    " + "public final " + type + " " + model.getName() + ";\n");
        }
        resultBuilder.append("\n    " + getSpace(tab) + "@JsonCreator\n");

        // Create Contructor
        StringBuilder contructor = new StringBuilder();
        contructor.append(getSpace(tab) + "    public " + name + "(");
        for (int i = 0; i < arrModel.size(); i++) {
            Model model = arrModel.get(i);
            String type;
            if (model.array) {
                type = "[]";
            } else {
                type = "";
            }
            if (i == arrModel.size() - 1) {
                contructor.append("\n            " + getSpace(tab) + "@JsonProperty(\"" + model.getKey() + "\") "
                        + model.getType() + " " + model.getName() + type + ") {\n");
            } else {
                contructor.append("\n            " + getSpace(tab) + "@JsonProperty(\"" + model.getKey() + "\") "
                        + model.getType() + " " + model.getName() + type + ",");
            }
        }
        for (int i = 0; i < arrModel.size(); i++) {
            Model model = arrModel.get(i);
            contructor.append("        " + getSpace(tab) + "this." + model.getName() + " = " + model.getName() + ";\n");
        }
        contructor.append(getSpace(tab) + "    }\n");
        resultBuilder.append(contructor.toString());

        StringBuilder childClass = new StringBuilder();
        int newTab = tab + 1;
        for (int i = 0; i < arrModel.size(); i++) {
            Model model = arrModel.get(i);
            if (!model.typeString) {
                childClass.append("\n");
                childClass.append(getSpace(tab) + process(model.getValue(), model.getType(), newTab));
                childClass.append("\n");
            }
        }
        resultBuilder.append(childClass.toString());

        // End Class
        resultBuilder.append(getSpace(tab) + "}");

//		System.out.println(resultBuilder.toString());
        return resultBuilder.toString();

    }

    public static Model createModel(String input) {
        Model model = new Model();
        int num = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '\"') {
                num += 1;
            }
            if (num == 2) {
                model.setKey(input.substring(1, i - 1));
                model.setValue(input.substring(i + 2, input.length() - 1));
            }
        }
        model.progress();
        return model;
    }

    static class Model {

        String key;
        String value;

        boolean typeString = true;
        boolean array = false;

        public void progress() {
            if (value.length() < 1) {
                return;
            }
            switch (this.value.charAt(0)) {
                case '{':
                    typeString = false;
                    break;
                case '[':
                    typeString = false;
                    array = true;
                    break;
            }
        }

        public Model() {
            this.key = "";
            this.value = "";
        }

        public Model(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getName() {
            StringBuilder result = new StringBuilder();
            result.append(key.substring(0, 1).toLowerCase());
            result.append(key.substring(1));
            return result.toString();
        }

        public String getKey() {
            return key;
        }

        public String getType() {
            if (typeString) {
                return "String";
            }
            StringBuilder result = new StringBuilder();
            result.append(key.substring(0, 1).toUpperCase());
            result.append(key.substring(1));
            return result.toString();
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String toString() {
            System.out.println("Key: " + this.key + "\n" + "Value: " + this.value);
            return "0";
        }
    }

}
