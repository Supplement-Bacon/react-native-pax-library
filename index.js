import { NativeModules } from "react-native";

const { Pax } = NativeModules;

export default {
    FULL_CUT: 0,
    PARTIAL_CUT: 1,

    printStr(text, cutMode) {
        return Pax.printStr(text, cutMode === undefined ? 0 : cutMode);
    },
    sayHi() {
        return Pax.sayHi();
    },
    openDrawer() {
        return Pax.openDrawer();
    },
    scanCode() {
        return Pax.scanCode();
    },
};
