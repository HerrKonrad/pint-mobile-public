package com.example.mobilepint.beneficios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.mobilepint.EnumCargos
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R

class FragmentDetalhesBeneficio : Fragment() {

    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalhes_beneficio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val txt_nome_label = view.findViewById(R.id.detalhes_beneficio_txt_nome_label) as TextView
        val txt_subtitulo_label = view.findViewById(R.id.detalhes_beneficio_txt_subtitulo_label) as TextView
        val txt_descricao_label = view.findViewById(R.id.detalhes_beneficio_txt_descricao_label) as TextView
        val linearLayout_imagem = view.findViewById(R.id.detalhes_beneficio_linearLayout_imagem) as LinearLayout
        val img_foto_beneficio = view.findViewById(R.id.detalhes_beneficio_img_foto_beneficio) as ImageView
        val btn_editar_beneficio = view.findViewById(R.id.detalhes_beneficio_btn_editar_beneficio) as Button

        txt_nome_label.text = GlobalVariables.detalhesNomeBeneficio
        txt_subtitulo_label.text = GlobalVariables.detalhesSubtituloBeneficio
        txt_descricao_label.text = GlobalVariables.detalhesDescricaoBeneficio

        if (numCargo == EnumCargos.ADMINISTRADOR.numCargo) {
            btn_editar_beneficio.visibility = View.VISIBLE
        } else {
            btn_editar_beneficio.visibility = View.GONE
        }

        if (GlobalVariables.detalhesEnderecoImagemBeneficio.isBlank()) {
            linearLayout_imagem.visibility = View.GONE
        } else {
            linearLayout_imagem.visibility = View.VISIBLE
            Glide.with(requireActivity())
                .load(GlobalVariables.detalhesEnderecoImagemBeneficio)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions().fitCenter())
                .into(img_foto_beneficio)
        }

        btn_editar_beneficio.setOnClickListener {
            if (numCargo == EnumCargos.ADMINISTRADOR.numCargo) {
                GlobalVariables.criarBeneficio = false
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentCriarBeneficio())
                fragment.commit()
            } else {
                Toast.makeText(requireContext(), "Esta funcionalidade requer privilégios adicionais.", Toast.LENGTH_LONG).show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerBeneficios())
            fragment.commit()
        }
    }
}