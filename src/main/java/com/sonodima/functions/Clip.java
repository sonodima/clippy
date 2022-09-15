package com.sonodima.functions;

import com.sonodima.network.Packet;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class Clip extends Thread {
    private final Clipboard clipboard;
    private LocalDateTime lastPacketTime;

    public Clip() {
        super();

        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        updateLastPacketTime();

        start();
    }

    @Override
    public void run() {
        Object oldData = null;

        while (isAlive()) {
            List<DataFlavor> flavors = Arrays.asList(clipboard.getAvailableDataFlavors());
            Transferable transferable = clipboard.getContents(null);

            // We want to ignore changes to the clipboard if they come from Clippy
            if (LocalDateTime.now().isAfter(lastPacketTime.plus(1000, ChronoUnit.MILLIS))) {
                try {
                    if (flavors.contains(DataFlavor.javaFileListFlavor)) {
                        // Handle file clipboard
                        Object data = transferable.getTransferData(DataFlavor.javaFileListFlavor);

                        if (!data.equals(oldData)) {
                            oldData = data;

                            Packet packet = new Packet(data, Packet.Type.files);
                            clipboardChangedListener.onClipboardChanged(packet);
                        }
                    } else if (flavors.contains(DataFlavor.stringFlavor)) {
                        // Handle string clipboard
                        Object data = transferable.getTransferData(DataFlavor.stringFlavor);

                        if (!data.equals(oldData)) {
                            oldData = data;

                            Packet packet = new Packet(data, Packet.Type.string);
                            clipboardChangedListener.onClipboardChanged(packet);
                        }
                    }
                } catch (UnsupportedFlavorException | IOException exception) {
                    exception.printStackTrace();
                }
            }  // Clipboard was recently updated by Clippy. Ignoring event.


            // Sleep for 100ms every iteration. We do not want to take a whole thread.
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) { }
        }
    }

    public void write(String data) {
        StringSelection selection = new StringSelection(data);
        clipboard.setContents(selection, selection);
    }

    public void write(List<File> files) {
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        if (operatingSystem.contains("mac")) {
            Script.runAppleScript(new String[]{
                    "set fileList to {" + Script.parseFiles(files) + "}",
                    "use framework \"AppKit\"",
                    "use Finder : application \"Finder\"",
                    "property this : a reference to current application",
                    "property NSString : a reference to NSString of this",
                    "property NSURL : a reference to NSURL of this",
                    "property NSMutableArray : a reference to NSMutableArray of this",
                    "property NSPasteboard : a reference to NSPasteboard of this",
                    "property NSFileManager : a reference to NSFileManager of this",
                    "clearClipboard()",
                    "writeClipboard(fileList)",
                    "to clearClipboard()",
                    "	set pasteBoard to NSPasteboard's generalPasteboard()",
                    "	pasteBoard's clearContents()",
                    "end clearClipboard",
                    "to writeClipboard(fileList)",
                    "	local fileList",
                    "	set fileURLs to NSMutableArray's array()",
                    "	set FileManager to NSFileManager's defaultManager()",
                    "	repeat with currentFile in fileList",
                    "		if currentFile's class = alias then set currentFile to currentFile's POSIX path",
                    "		set currentFilePath to (NSString's stringWithString:currentFile)'s stringByStandardizingPath()",
                    "		if (FileManager's fileExistsAtPath:currentFilePath) then (fileURLs's addObject:(NSURL's fileURLWithPath:currentFilePath))",
                    "	end repeat",
                    "	set pasteBoard to NSPasteboard's generalPasteboard()",
                    "	pasteBoard's writeObjects:fileURLs",
                    "end writeClipboard",
            });
        } else if (operatingSystem.contains("win")) {
            Script.runPowerShellScript(new String[]{
                    "Add-Type -AssemblyName System.Windows.Forms",
                    "[System.Collections.Specialized.StringCollection]$fileList = @(" + Script.parseFiles(files) + ")",
                    "[System.Windows.Forms.Clipboard]::SetFileDropList($fileList)"
            });
        }
    }

    public void updateLastPacketTime() {
        lastPacketTime = LocalDateTime.now();
    }

    public interface ClipboardChangedListener {
        void onClipboardChanged(Packet packet);
    }

    private ClipboardChangedListener clipboardChangedListener;

    public void setClipboardChangedListener(ClipboardChangedListener listener) {
        this.clipboardChangedListener = listener;
    }
}
