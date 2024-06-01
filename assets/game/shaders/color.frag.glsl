#ifdef GL_ES
precision mediump float;
#endif
uniform vec4 u_color;
uniform sampler2D u_texture;
varying vec2 v_texCoords;

void main() {
    vec4 color = texture(u_texture, v_texCoords);
    color.r = u_color.r;
    color.g = u_color.g;
    color.b = u_color.b;
    gl_FragColor = color;
}
