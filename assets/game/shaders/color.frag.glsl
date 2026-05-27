#ifdef GL_ES
precision mediump float;
#endif
uniform vec4 u_color;
uniform sampler2D u_texture;
varying vec2 v_texCoords;
varying vec4 v_color;

void main() {
    vec4 color = texture2D(u_texture, v_texCoords);
    gl_FragColor = vec4(u_color.rgb, color.a * v_color.a);
}
