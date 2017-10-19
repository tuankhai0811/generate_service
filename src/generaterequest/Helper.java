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
            case 6:
                result = "                        ";
                break;
            case 7:
                result = "                            ";
                break;
            case 8:
                result = "                                ";
                break;
            case 9:
                result = "                                    ";
                break;
            case 10:
                result = "                                        ";
                break;
            case 11:
                result = "                                            ";
                break;
            default:
                result = "";
        }
        return result;
    }

    public static String process(String source, String nameClass, int tab, boolean isImp) {
        ArrayList<Model> arrModel = new ArrayList<>();
        ArrayList<String> list = new ArrayList<>();
        StringBuilder output = new StringBuilder();
        String imp = "";
        String saveSource = source;
        if (nameClass.isEmpty()) {
            nameClass = "Example";
        }
        if (isImp) {
            imp = " implements Serializable";
        }

        // Xử lý chuỗi đầu tiên
        if (tab == 0) {
            // Bỏ ký tự enter
            source = source.replace((char) 13, ' ');
            // Bỏ ký tự fetch line
            source = source.replace('\n', ' ');
            source = source.replaceAll("\n", "");
            source = source.replaceAll("\t", "");
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
//		System.out.println("Chuoi ban dau [" + tab + "]: " + source);

        // Kiểm tra chuỗi là array hay object
        if (source.charAt(1) == '[') {
            // Nếu là array thì tách object ra
            int start = 0, end = start;
            int numOpen = 0;
            int numClose = 0;
            for (int i = 0; i < source.length() - 1; i++) {
                if (source.charAt(i) == '{') {
                    numOpen += 1;
                    if (numOpen == 1) {
                        start = i - 1;
                    }
                }
                if (source.charAt(i) == '}') {
                    numClose += 1;
                }
                if (numOpen == numClose && numOpen != 0) {
                    end = i + 1;
                    break;
                }
            }
            source = source.substring(start, end);
//			System.out.println("Chuoi da tach [" + tab + "]: " + source);
        }

        // Tách từng model
        for (int index = 2; index < source.length() - 2; index++) {
            int start = index, end = start;
            int numOpen = 0;
            int numClose = 0;
            int numSymbol = 0;
            boolean has = false, isString = true, isNull = false;
            for (int i = index; i < source.length() - 2; i++) {
                // Đếm số ký tự \"
                if (source.charAt(i) == '\"') {
                    numSymbol += 1;
                }
                // Kiểm tra đã đã gặp dấu : chưa
                if (source.charAt(i) == ':' && numSymbol == 2) {
                    has = true;
                    // Kiểm tra value là array, object hay String
                    if (source.charAt(i + 2) == '{' || source.charAt(i + 2) == '[') {
                        isString = false;
                    }
                    // Kiểm tra value có là null hay không
                    if (source.charAt(i + 1) != '\"') {
                        isNull = true;
                    }
                    continue;
                }
                // Nếu đã gặp ký tự : thì bắt đầu đếm dấu ngoặc
                // Nếu chuỗi sau ko phải array hoặc object thì đếm dấu "
                if (has) {
                    if (isString) {
                        // Kết thúc một chuỗi model
                        if (isNull) {
                            if (source.charAt(i) == ',' || source.charAt(i) == '}' || source.charAt(i) == ']') {
                                list.add(source.substring(start, i));
                                index = i;
                                break;
                            }
                        } else {
                            if (numSymbol == 4) {
                                list.add(source.substring(start, i + 1));
                                index = i + 1;
                                break;
                            }
                        }
                    } else {
                        if (source.charAt(i) == '{' || source.charAt(i) == '[') {
                            numOpen += 1;
                        }
                        if (source.charAt(i) == '}' || source.charAt(i) == ']') {
                            numClose += 1;
                        }
                        if (numOpen == numClose && numOpen != 0) {
                            list.add(source.substring(start, i + 2));
                            // Kiểm tra đã hết model trong object chưa
                            if (source.charAt(i + 1) == ',') {
                                // Còn model
                                index = i + 3;
                            } else {
                                // Hết model
                                index = i + 2;
                            }
                            break;
                        }
                    }
                }
            }
        }
        // End tach model

        // Xử lý model
        for (String item : list) {
            arrModel.add(createModel(item));
        }

        // System.out.println("List string model");
        // for(String item:list){
        // System.out.println(item);
        // }
//		System.out.println("List model " + tab);
//		for (Model item : arrModel) {
//			item.toString();
//		}
        // Create Class
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(getSpace(tab) + "public final class " + nameClass + imp + " {\n");
        // Khai báo biến
        for (int i = 0; i < arrModel.size(); i++) {
            Model model = arrModel.get(i);
            String type;
            if (model.isArray) {
                type = model.getType() + "[]";
            } else {
                type = model.getType();
            }
            resultBuilder.append(getSpace(tab + 1) + "public final " + type + " " + model.getName() + ";\n");
        }

        // Create Contructor
        resultBuilder.append("\n" + getSpace(tab + 1) + "@JsonCreator\n");
        StringBuilder contructor = new StringBuilder();
        contructor.append(getSpace(tab + 1) + "public " + nameClass + "(");
        if (arrModel.size() == 1) {
            Model model = arrModel.get(0);
            String type;
            if (model.isArray) {
                type = "[]";
            } else {
                type = "";
            }
            contructor.append("@JsonProperty(\"" + model.getKey() + "\") " + model.getType() + " " + model.getName()
                    + type + ") {\n");
        } else {
            for (int i = 0; i < arrModel.size(); i++) {
                Model model = arrModel.get(i);
                String type;
                if (model.isArray) {
                    type = "[]";
                } else {
                    type = "";
                }
                if (i == arrModel.size() - 1) {
                    contructor.append("\n" + getSpace(tab + 3) + "@JsonProperty(\"" + model.getKey() + "\") "
                            + model.getType() + " " + model.getName() + type + ") {\n");
                } else {
                    contructor.append("\n" + getSpace(tab + 3) + "@JsonProperty(\"" + model.getKey() + "\") "
                            + model.getType() + " " + model.getName() + type + ",");
                }
            }
        }
        for (int i = 0; i < arrModel.size(); i++) {
            Model model = arrModel.get(i);
            contructor.append(getSpace(tab + 2) + "this." + model.getName() + " = " + model.getName() + ";\n");
        }
        if (arrModel.size() == 0) {
            contructor.append(getSpace(tab) + ") {\n");
        }
        contructor.append(getSpace(tab + 1) + "}\n");
        resultBuilder.append(contructor.toString());
        // End contructor

        // Create child class
        StringBuilder childClass = new StringBuilder();
        for (int i = 0; i < arrModel.size(); i++) {
            Model model = arrModel.get(i);
            if (!model.isString) {
                childClass.append("\n");
                childClass.append(getSpace(tab) + process(model.getValue(), model.getType(), tab + 1, isImp));
                childClass.append("\n");
            }
        }
        resultBuilder.append(childClass.toString());
        // End child class

        // End Class
        resultBuilder.append(getSpace(tab) + "}");

//		System.out.println(resultBuilder.toString());
        return resultBuilder.toString();
    }

    public static Model createModel(String input) {
        Model model = new Model();
        // num char \"
        int num = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '\"') {
                num += 1;
            }
            if (num == 2) {
                model.setKey(input.substring(1, i));
                model.setValue(input.substring(i + 2, input.length()));
                break;
            }
        }
        model.progress();
        return model;
    }

    static class Model {

        private String key;
        private String value;
        boolean isString = true;
        boolean isArray = false;

        public void progress() {
            if (value.length() < 1) {
                return;
            }
            switch (this.value.charAt(1)) {
                case '{':
                    isString = false;
                    break;
                case '[':
                    for (char i : value.toCharArray()) {
                        if (i == '{') {
                            isString = false;
                            break;
                        }
                    }
                    isArray = true;
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
            if (isString) {
                return "String";
            }
            StringBuilder result = new StringBuilder();
            result.append(key.substring(0, 1).toUpperCase());
            result.append(key.substring(1));
            return result.toString();
        }

        public void setKey(String key) {
            this.key = key.trim();
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value.trim();
        }

        public String toString() {
            System.out.println("Key: " + this.key + "\n" + "Value: " + this.value);
            System.out.println("Is String: " + isString + " Is Array: " + isArray);
            return "";
        }
    }
}
