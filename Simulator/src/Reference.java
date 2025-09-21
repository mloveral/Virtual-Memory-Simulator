public class Reference {
    int page;
    int offset;
    char type; // 'r' o 'w'
    Reference(int page, int offset, char type) {
        this.page = page;
        this.offset = offset;
        this.type = type;
    }
}

