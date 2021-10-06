package jp.co.syslinks.sscce.java.java8;

import java.util.Optional;

public class Java8Optional {

    public static void main(String[] args) {
        Layer3 layer3 = new Layer3();
        layer3.value = "data3";
        Layer2 layer2 = new Layer2();
        layer2.layer3 = layer3;
        Layer1 layer1 = new Layer1();
        layer1.layer2 = layer2;
        System.out.println(layer1.layer2.layer3.value);

        System.out.println(Optional.ofNullable(layer1).map(t -> t.layer2).map(t -> t.layer3).map(t -> t.value).orElse(null));

        layer3.value = null;
        System.out.println(Optional.ofNullable(layer1).map(t -> t.layer2).map(t -> t.layer3).map(t -> t.value).orElse(null));

        layer3 = null;
        System.out.println(Optional.ofNullable(layer1).map(t -> t.layer2).map(t -> t.layer3).map(t -> t.value).orElse(null));

        layer2 = null;
        System.out.println(Optional.ofNullable(layer1).map(t -> t.layer2).map(t -> t.layer3).map(t -> t.value).orElse(null));

        layer1 = null;
        System.out.println(Optional.ofNullable(layer1).map(t -> t.layer2).map(t -> t.layer3).map(t -> t.value).orElse(null));
    }

    static class Layer1 {
        public Layer2 layer2;
    }

    static class Layer2 {
        public Layer3 layer3;
    }

    static class Layer3 {
        String value;
    }
}
