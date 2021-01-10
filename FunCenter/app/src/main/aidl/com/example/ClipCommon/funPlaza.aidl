// funPlaza.aidl
package com.example.ClipCommon;

interface funPlaza {

    String[] getSongList();
    Bitmap getImages(int i);
    void playSongAtId(int i);
    void pause();
    void resume();
    void closeService();
    void stop();
    int test();

}
