module Jekyll

  class SeriesTag < Liquid::Tag
    def initialize(tag_name, params, tokens)
      super
    end

    def render(context)
      site = context.registers[:site]
      page_data = context.environments.first["page"]
      series_name = page_data['series']
      if !series_name
        puts "Unable to find series name for page: #{page.title}"
        return "<!-- Error with series tag -->"
      end

      all_entries = []
      site.posts.each do |p|
        if p.data['series'] == series_name
          all_entries << p
        end
      end

      all_entries.sort_by { |p| p.date.to_f }

      text = "<div class='panel seriesNote'>"
      list = "<ul>"
      all_entries.each_with_index do |post, idx|
        list += "<li><strong>Part #{idx+1}</strong> - "
        if post.data['title'] == page_data['title']
          list += "This Article"
          text += "<p>This article is <strong>Part #{idx+1}</strong> in a <strong>#{all_entries.size}-Part</strong> Series.</p>"
        else
          list += "<a href='#{post.url}'>#{post.data['title']}</a>"
        end
        list += "</li>"
      end
      text += list += "</ul></div>"
      
    end
  end
end

Liquid::Template.register_tag('series_list', Jekyll::SeriesTag)