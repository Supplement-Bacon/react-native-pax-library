declare var Pax: {
    FULL_CUT: number;
    PARTIAL_CUT: number;

    printStr: (text: string, cutMode?: number) => Promise<boolean>;
    sayHi: () => Promise<string>;
    openDrawer: () => Promise<any>;
    scanCode: () => Promise<string>;
};

export default Pax;
