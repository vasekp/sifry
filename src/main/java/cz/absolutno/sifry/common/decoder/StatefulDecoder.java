package cz.absolutno.sifry.common.decoder;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.preference.PreferenceManager;
import android.util.SparseArray;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

public final class StatefulDecoder extends Decoder {

    private OnStateChangedListener onStateChangedListener = null;

    private final SparseArray<ChainDecoder> decoders = new SparseArray<>();
    private ChainDecoder defDecoder = null;
    private ChainDecoder curDecoder = null;
    private ChainDecoder tmpDecoder = null;

    private int format = 0;
    private int defaultFormat = 0;
    private String backupState = null;
    private boolean decoderClaimed;

    private static final class Rule {
        private final int ref;
        private final int code;
        private final int format;
        private final int after;
        private final String state;
        private final String tmpState;

        private Rule(int ref, int code, int format, int after, String state, String tmpState) {
            super();
            this.ref = ref;
            this.code = code;
            this.format = format;
            this.after = after;
            this.state = state;
            this.tmpState = tmpState;
        }
    }

    private final class RuleDecoder extends ChainDecoder {

        private final ArrayList<Rule> rules = new ArrayList<>();
        private final String state;
        private final boolean temp;
        private final int codeDesc;

        RuleDecoder(String state, boolean temp, int codeDesc) {
            this.state = state;
            this.temp = temp;
            this.codeDesc = codeDesc;
        }

        void newRule(int ref, int code, int format, int after, String state, String tmpState) {
            rules.add(new Rule(ref, code, format, after, state, tmpState));
        }

        @Override
        protected boolean isTemp() {
            return temp;
        }

        @Override
        public String decode(int x) {
            for (Rule r : rules) {
                if (r.code == x) {
                    if (r.format > 0)
                        newFormat(r.format);
                    if (r.ref != 0) {
                        ChainDecoder ref = decoders.get(r.ref);
                        if (ref.isTemp()) {
                            if (r.after != 0)
                                newCurDecoder(decoders.get(r.after), r.state);
                            newTmpDecoder(ref, r.tmpState);
                        } else
                            newCurDecoder(ref, r.state);
                    }
                    return "";
                } else if (r.code < 0) {
                    Decoder d = decoders.get(r.ref);
                    String s = d.decode(x);
                    if (s != null) {
                        //android.util.Log.d("rd", String.format("rule %s succeeded", App.getContext().getResources().getResourceName(r.ref)));
                        if (r.format > 0)
                            newFormat(r.format);
                        //android.util.Log.d("rd", String.format("claimed %s temp %s after %s", Boolean.toString(decoderClaimed), Boolean.toString(temp), r.after != 0 ? App.getContext().getResources().getResourceName(r.after) : "0"));
                        if (!decoderClaimed) {
                            if (r.after != 0)
                                newCurDecoder(decoders.get(r.after), r.state);
                            else if (!isTemp())
                                newCurDecoder(this, r.state);
                        } else if (r.state != null)
                            newState(r.state, false);
                        return s;
                    }
                }
            }
            return null;
        }

        @Override
        public String getDesc(int x) {
            for (Rule r : rules) {
                String s = null;
                if (r.code == x) {
                    if (codeDesc == 0) continue;
                    s = decoders.get(codeDesc).getDesc(x);
                } else if (r.code < 0) {
                    if (r.ref == 0) continue;
                    s = decoders.get(r.ref).getDesc(x);
                }
                if (s != null && s.length() != 0)
                    return s;
            }
            return null;
        }

        @Override
        public EncodeResult encodeSingle(String s, int ix, boolean prefix) {
            EncodeResult er;
            for (Rule r : rules) {
                if (prefix && r.code >= 0) continue;
                ChainDecoder d = decoders.get(r.ref);
                er = d.encodeSingle(s, ix, prefix || (r.code >= 0));
                if (er == null) continue;
                if (!decoderClaimed) {
                    if (r.after != 0) {
                        curDecoder = decoders.get(r.after);
                        decoderClaimed = true;
                    } else if (!d.isTemp()) {
                        curDecoder = d;
                        decoderClaimed = true;
                    }
                }
                if (r.code >= 0)
                    return new EncodeResult(er, r.code);
                else
                    return er;
            }
            return null;
        }

        @Override
        protected boolean encodeInternal(String s, ArrayList<Integer> list) {
            final int len = s.length();
            boolean err = false;
            int ix;
            for (ix = 0; ix < len; ix++) {
                decoderClaimed = false;
                EncodeResult er = curDecoder.encodeSingle(s, ix, false);
                if (er == null) {
                    err = true;
                    continue;
                }
                if (er.prefix >= 0)
                    list.add(er.prefix);
                list.add(er.code);
                ix += er.len - 1;
            }
            return err;
        }

    }


    public StatefulDecoder(int resId) {
        super();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        XmlResourceParser xml = App.getContext().getResources().getXml(resId);
        RuleDecoder rd = null;
        try {
            int entry = 0;
            int id = 0;
            int codeDescGlobal = 0;
            for (xml.next(); xml.getEventType() != XmlPullParser.END_DOCUMENT; xml.next()) {
                if (xml.getEventType() == XmlPullParser.START_TAG) {
                    switch (xml.getName()) {
                        case "state-decoder":
                            entry = xml.getAttributeResourceValue(null, "entrypoint", 0);
                            String pref = xml.getAttributeValue(null, "format_flag");
                            codeDescGlobal = xml.getAttributeResourceValue(null, "code_desc", 0);
                            if (pref != null && sp != null && sp.getBoolean(pref, false))
                                defaultFormat = R.id.idFormatLowerCase;
                            else
                                defaultFormat = R.id.idFormatUpperCase;
                            if (codeDescGlobal != 0)
                                decoders.put(codeDescGlobal, new StringDecoder(codeDescGlobal));
                            break;
                        case "rule-array": {
                            id = xml.getAttributeResourceValue(null, "id", 0);
                            int codeDesc = xml.getAttributeResourceValue(null, "code_desc", codeDescGlobal);
                            String state = xml.getAttributeValue(null, "state");
                            boolean temp = xml.getAttributeBooleanValue(null, "temp", false);
                            rd = new RuleDecoder(state, temp, codeDesc);
                            if (codeDesc != codeDescGlobal)
                                decoders.put(codeDesc, new StringDecoder(codeDesc));
                            break;
                        }
                        case "rules":
                        case "strings":
                        case "special": {
                            int ref = xml.getAttributeResourceValue(null, "ref", 0);
                            int code = xml.getAttributeIntValue(null, "code", -1);
                            String acc = xml.getAttributeValue(null, "access");
                            boolean accdef = xml.getAttributeBooleanValue(null, "access_def", false);
                            int after = xml.getAttributeResourceValue(null, "after", 0);
                            String state = xml.getAttributeValue(null, "state");
                            String tmpstate = xml.getAttributeValue(null, "tmp_state");
                            int format = xml.getAttributeResourceValue(null, "format", 0);
                            if (xml.getName().equals("strings"))
                                decoders.put(ref, new StringDecoder(ref));
                            if (acc == null || (sp != null ? sp.getBoolean(acc, accdef) : accdef)) {
                                assert rd != null;
                                rd.newRule(ref, code, format, after, state, tmpstate);
                            }
                            break;
                        }
                    }
                } else if (xml.getEventType() == XmlPullParser.END_TAG)
                    if (xml.getName().equals("rule-array"))
                        decoders.put(id, rd);
            }
            defDecoder = decoders.get(entry);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        clearState();
    }

    private void newCurDecoder(ChainDecoder d, String state) {
        //android.util.Log.d("rd", String.format("setting cur %s", App.getContext().getResources().getResourceName(decoders.keyAt(decoders.indexOfValue(d)))));
        curDecoder = d;
        decoderClaimed = true;
        if (state == null && d instanceof RuleDecoder)
            state = ((RuleDecoder) d).state;
        if (state == null)
            state = backupState;
        //android.util.Log.d("rd", String.format("curstate \"%s\"", state != null ? state : "null"));
        newState(state, false);
    }

    private void newTmpDecoder(ChainDecoder d, String state) {
        //android.util.Log.d("rd", String.format("setting tmp %s", App.getContext().getResources().getResourceName(decoders.keyAt(decoders.indexOfValue(d)))));
        tmpDecoder = d;
        if (state == null && d instanceof RuleDecoder)
            state = ((RuleDecoder) d).state;
        //android.util.Log.d("rd", String.format("tmpstate \"%s\"", state != null ? state : "null"));
        newState(state, true);
    }

    private void newState(String state, boolean temp) {
        if (state == null) return;
        if (!temp) backupState = state;
        if (!temp && tmpDecoder != null) return;
        if (onStateChangedListener != null)
            onStateChangedListener.onStateChanged(state);
    }

    private void restoreDecoder() {
        tmpDecoder = null;
        newCurDecoder(curDecoder, backupState);
    }

    private void newFormat(int format) {
        //android.util.Log.d("rd", String.format("new format %s", App.getContext().getResources().getResourceName(format)));
        if (format == R.id.idFormatDefault)
            format = defaultFormat;
        switch (format) {
            case R.id.idFormatUpperCase:
            case R.id.idFormatLowerCase:
            case R.id.idFormatSentence:
                this.format = format;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void clearState() {
        tmpDecoder = null;
        newFormat(R.id.idFormatDefault);
        newCurDecoder(defDecoder, null);
    }

    @Override
    @SuppressLint("DefaultLocale")
    public String decode(int x) {
        String s;
        decoderClaimed = false;
        if (tmpDecoder != null) {
            s = tmpDecoder.decode(x);
            restoreDecoder();
        } else
            s = curDecoder.decode(x);
        if (s != null) {
            switch (format) {
                case R.id.idFormatLowerCase:
                    return s.toLowerCase();
                case R.id.idFormatSentence:
                    if (s.length() >= 1) {
                        format = R.id.idFormatLowerCase;
                        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
                    } else
                        return s;
                default:
                    return s.toUpperCase();
            }
        } else
            return "?";
    }

    @Override
    public String decode(ArrayList<Integer> list) {
        clearState();
        return super.decode(list);
    }

    @Override
    public String getDesc(int x) {
        String s;
        if (tmpDecoder != null)
            s = tmpDecoder.getDesc(x);
        else
            s = curDecoder.getDesc(x);
        if (s != null && s.length() > 0)
            return s;
        else
            return "?";
    }

    @Override
    protected boolean encodeInternal(String s, ArrayList<Integer> list) {
        clearState();
        return defDecoder.encode(s, list);
    }

    public void setOnStateChangedListener(OnStateChangedListener scl) {
        this.onStateChangedListener = scl;
    }

    public interface OnStateChangedListener {
        void onStateChanged(String state);
    }

}
