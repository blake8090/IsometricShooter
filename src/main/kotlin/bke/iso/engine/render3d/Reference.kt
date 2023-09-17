package bke.iso.engine.render3d

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder

private fun newRect(): ModelInstance {
    val width = 1f
    val height = 1f
    val builder = ModelBuilder()
    builder.begin()
    builder.node()
    val mpb: MeshPartBuilder = builder.part(
        "rect",
        GL20.GL_TRIANGLES,
        VertexAttributes.Usage.Position.toLong().or(VertexAttributes.Usage.Normal.toLong()),
        Material(ColorAttribute.createDiffuse(Color.GREEN))
    )
//        mpb.rect(
//            Vector3(0f, 0f, 0f),
//            Vector3(0f, 1f, 0f),
//            Vector3(1f, 1f, 0f),
//            Vector3(1f, 0f, 0f),
//            Vector3(0f, 0f, -1f)
//        )
    mpb.rect(
        -(width*0.5f), -(height*0.5f), 0f,
        (width*0.5f), -(height*0.5f), 0f,
        (width*0.5f), (height*0.5f), 0f,
        -(width*0.5f), (height*0.5f), 0f,
        0f, 0f, -1f)
    return ModelInstance(builder.end())
}

/*
texture can be added as attribute to material:
material.set(new TextureAttribute(TextureAttribute.Diffuse, texture)

for transparent plane that has alpha add to other attribute:
attributes.add( new BlendingAttribute(color.getFloat(3)));
attributes.add( new FloatAttribute(FloatAttribute.AlphaTest, 0.5f));
material.set(attributes);

Init the ModelInstance to get model that returned:
modelInstance = new ModelInstance(createPlaneModel(...))

 */