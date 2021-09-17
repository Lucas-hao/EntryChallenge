public class Main {

    public static void main(String[] args) {
        String input = "{\n" +
                "\n" +
                "\"shift\": {\n" +
                "\n" +
                "    \"start\": \"2038-01-01T20:15:00\",\n" +
                "    \n" +
                "    \"end\": \"2038-01-02T05:16:00\"\n" +
                "\n" +
                "},\n" +
                "\n" +
                "\"roboRate\": {\n" +
                "\n" +
                "    \"standardDay\": {\n" +
                "    \n" +
                "      \"start\": \"07:00:00\",\n" +
                "    \n" +
                "      \"end\": \"23:00:00\",\n" +
                "    \n" +
                "      \"value\": 20\n" +
                "    \n" +
                "    },\n" +
                "    \n" +
                "    \"standardNight\": {\n" +
                "    \n" +
                "      \"start\": \"23:00:00\",\n" +
                "    \n" +
                "      \"end\": \"07:00:00\",\n" +
                "    \n" +
                "      \"value\": 25\n" +
                "    \n" +
                "    },\n" +
                "    \n" +
                "    \"extraDay\": {\n" +
                "    \n" +
                "      \"start\": \"07:00:00\",\n" +
                "    \n" +
                "      \"end\": \"23:00:00\",\n" +
                "    \n" +
                "      \"value\": 30\n" +
                "    \n" +
                "    },\n" +
                "    \n" +
                "    \"extraNight\": {\n" +
                "    \n" +
                "      \"start\": \"23:00:00\",\n" +
                "    \n" +
                "      \"end\": \"07:00:00\",\n" +
                "    \n" +
                "      \"value\": 35\n" +
                "    \n" +
                "    }\n" +
                "\n" +
                "}\n" +
                "}";
        RobotWork work = new RobotWork(input);
        work.calculateValue();
        System.out.println(work.getValue());
    }
}
