
setLocal

set jdkbase=C:\ZZ-java-tools\jdk-18.0.2.1
set jfxbase=C:\ZZ-java-tools\javafx-sdk-18.0.2

set jfx=%jfxbase%\bin
set jfxlibs=%jfxbase%\lib
set jdk=%jdkbase%\bin

set mainlib=C:\ZZ-Data-Science\javafx-convolution-network\target\classes
set data=C:\ZZ-Data-Science\datasets

set mylibs=%jfxlibs%\*;./lib/*;%data%;.\src\main\resources;%mainlib%
set mycp=.;./src;./build;%mylibs%


%jdk%\java --module-path %jfxlibs% --add-modules=javafx.controls -cp %mycp% datasci.frontend.ctrl.FxConvoMain

endLocal



